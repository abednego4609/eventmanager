package com.halusinogen.eventmanager.manager.impl;

import com.halusinogen.eventmanager.event.intf.Event;
import com.halusinogen.eventmanager.event.intf.EventHandler;
import com.halusinogen.eventmanager.manager.exception.EventHandlerAlreadyRegisteredException;
import com.halusinogen.eventmanager.manager.intf.EventManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 *
 * @author marchell
 */
public class DefaultEventManager implements EventManager{
    private final static Logger LOGGER = Logger.getLogger(DefaultEventManager.class.getName());
    private static DefaultEventManager n;
    
    private final Queue<Event> bigQueue;
    private final Map<Class<? extends Event>, Set<EventHandler>> classAndHandlerMapping;
    private final Map<Class<? extends Event>, Queue<Event>> classAndLittleQueueMapping;
    private final Map<Class<? extends Event>, Thread> classAndThreadMapping;

    private final Set<EventHandler> stillValidHandlers;
    private boolean isRun = true;


    private DefaultEventManager() {

        bigQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();
        classAndHandlerMapping = new HashMap<>();
        classAndLittleQueueMapping = new HashMap<>();
        stillValidHandlers = Collections.synchronizedSet(new HashSet<EventHandler>());
        classAndThreadMapping = new ConcurrentHashMap<>();

        new Thread(classifierRunnable,"BIG QUEUE").start();
        LOGGER.info("BIG QUEUE is started");
        
    }
    
    public static DefaultEventManager getInstance(){
        if(n == null){
            n = new DefaultEventManager();
        }
        
        return n;
        
    }

    @Override
    public <T extends Event> void register(Class<T> event, EventHandler<T> handler) throws EventHandlerAlreadyRegisteredException{

        if(Event.class.equals(event.getClass()))throw new IllegalArgumentException("Can't publish event of type Event. Must use subclass of Event");

        if(stillValidHandlers.contains(handler)){
            throw new EventHandlerAlreadyRegisteredException("EventHandler already registered.");
        }
        if(!classAndHandlerMapping.containsKey(event)){

            classAndHandlerMapping.put(event, new HashSet<>());
        }
        LOGGER.info("Registering a " + event.getName() + " event handler : hashcode : " + handler);
        classAndHandlerMapping.get(event).add(handler);
    }

    @Override
    public <T extends Event> void unregister(EventHandler<T> handler) {
        LOGGER.info("Unregister a "+handler.getClass().getName()+" event handler : hashcode "+handler.hashCode());
        stillValidHandlers.remove(handler);
    }

    @Override
    public void publish(Event event) {
        LOGGER.info("Publishing a "+event.getClass().getName()+" event. hashcode : "+event);

        if(Event.class.equals(event.getClass()))throw new IllegalArgumentException("Can't publish event of type Event. Must use subclass of Event");

        bigQueue.add(event);
    }

    @Override
    public void turnOff() {
        this.isRun = false;
    }

    private Runnable classifierRunnable = new Runnable() {
        private final Logger LOGGER = Logger.getLogger("classifierRunnable");
        @Override
        public void run() {
            while(isRun){
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                }
                //Retrieve an event
                Event headEvent= bigQueue.poll();

                // bigQueue still empty, move on
                if(headEvent==null){
                    LOGGER.info("bigQueue still empty, move on");
                    continue;
                }
                final Class<? extends Event> headEventClass = headEvent.getClass();

                // No listener registered for this event, move on.
                if(!classAndHandlerMapping.containsKey(headEventClass)){
                    LOGGER.info("No listener registered for "+headEventClass.getName()+" event, move on.");
                    continue;
                }

                //Event is not null, and got listener(s). Classify that.
                if(!classAndLittleQueueMapping.containsKey(headEventClass)) {
                    classAndLittleQueueMapping.put(headEventClass, new ConcurrentLinkedQueue<>());
                }
                classAndLittleQueueMapping.get(headEventClass).add(headEvent);
                LOGGER.info("Good candidate :  "+headEventClass.getName()+" : "+headEvent.hashCode());

                if(!classAndThreadMapping.containsKey(headEventClass) || classAndThreadMapping.get(headEventClass).getState().compareTo(Thread.State.TERMINATED)==0){
                    // Make a thread for that event class if it does not exist OR is terminated
                    LOGGER.info("Make a thread because "+headEventClass.getName()+" class does not exist OR is terminated");
                    Thread dispatcher = new Thread(new Runnable() {
                        private final Logger LOGGER = Logger.getLogger("dispatcher");
                        @Override
                        public void run() {

                            //For every events,..
                            Queue<Event> q = classAndLittleQueueMapping.get(headEventClass);
                            LOGGER.info("Got a queue of Event with size of "+q.size());
                            do {
                                final Event event = q.poll();
                                // ...dispatch that event to every handler
                                LOGGER.info("Gonna dispatch "+event.getClass().getName()+" to every handler very soon...");
                                classAndHandlerMapping
                                        .get(headEventClass)
                                        .parallelStream()
                                        .forEach(
                                                (handler) -> {
                                                    LOGGER.info("Dispatching "+event.getClass().getName()+" to  "+handler.getClass().getName()+" handler. : "+handler.hashCode());
                                                    handler.handleEvent(event);
                                                }
                                        );
                                try {
                                    Thread.sleep(10L);
                                } catch (InterruptedException e) {
                                }
                            }while(!q.isEmpty());
                        }
                    }, "EventDispatcher for "+headEventClass.getName());
                    classAndThreadMapping.put(headEventClass, dispatcher);
                    dispatcher.start();
                }

            }
            
        }
    };

}
