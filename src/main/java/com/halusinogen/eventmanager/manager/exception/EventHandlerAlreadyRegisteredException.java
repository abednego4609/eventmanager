package com.halusinogen.eventmanager.manager.exception;

/**
 *
 * @author marchell
 */
public class EventHandlerAlreadyRegisteredException extends Exception{

    public EventHandlerAlreadyRegisteredException() {
    }

    public EventHandlerAlreadyRegisteredException(String message) {
        super(message);
    }

    public EventHandlerAlreadyRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventHandlerAlreadyRegisteredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    

}
