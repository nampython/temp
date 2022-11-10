package org.example.exceptions;

public class BeanInstantiationException extends InstantiationException{
    public BeanInstantiationException(String message) {
        super(message);
    }

    public BeanInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}
