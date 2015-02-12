/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.halusinogen.eventmanager.event.intf;

/**
 *
 * @author marchell
 * @param <E>
 */
public interface EventHandler<E extends Event> {
    
    public void handleEvent(E event);
    
}
