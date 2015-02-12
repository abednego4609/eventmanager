package com.halusinogen.eventmanager.manager.impl;

import com.halusinogen.eventmanager.manager.exception.EventHandlerAlreadyRegisteredException;
import com.halusinogen.eventmanager.manager.impl.kejadian.*;
import net.jodah.concurrentunit.Waiter;
import org.junit.Test;

public class DefaultEventManagerTest {

    @Test
    public void integrityTest() throws Throwable {
        final Waiter w = new Waiter();
        DefaultEventManager eventManager = DefaultEventManager.getInstance();

        Runnable r1= new Runnable() {
            @Override
            public void run() {
                for (int i= 0 ; i< 500000; i++) {
                    try {
                        eventManager.register(Banjir.class, event -> {
                            w.resume();
                        });
                        eventManager.register(HujanAsam.class, event -> w.resume());
                        eventManager.register(Kebakaran.class, event -> {
                            w.resume();
                        });
                        eventManager.register(Kiamat.class, event -> {
                            w.resume();
                        });

                        eventManager.register(Ledakan.class, event -> {
                            w.resume();
                        });
                    } catch (EventHandlerAlreadyRegisteredException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(10L);
                    }catch (Exception w){}
                }
            }
        };

        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                for (int i= 0 ; i< 400; i++){
                eventManager.publish(new Kebakaran());
                eventManager.publish(new HujanAsam());
                eventManager.publish(new Kiamat());
                eventManager.publish(new Ledakan());
                eventManager.publish(new Banjir());
                    try {
                        Thread.sleep(10L);
                    }catch (Exception w){}

                }
            }
        };


        new Thread(r1).start();
        new Thread(r2).start();

        w.await( 999999999,999999999);

    }


}