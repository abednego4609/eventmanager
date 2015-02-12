package com.halusinogen.eventmanager.manager.intf;

import com.halusinogen.eventmanager.event.intf.Event;
import com.halusinogen.eventmanager.event.intf.EventHandler;
import com.halusinogen.eventmanager.manager.exception.EventHandlerAlreadyRegisteredException;

/**
 *
 * @author marchell
 */
public interface EventManager {
    
    public <T extends Event> void register(Class<T> event, EventHandler<T> handler) throws EventHandlerAlreadyRegisteredException;
    
    public <T extends Event> void unregister(EventHandler<T> handler);
    
    public void publish(Event event);

    public void turnOff();

}
