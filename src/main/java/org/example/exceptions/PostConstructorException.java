package org.example.exceptions;

public class PostConstructorException extends ServiceInstantiationException{
    public PostConstructorException(String message) {
        super(message);
    }

    public PostConstructorException(String message, Throwable cause) {
        super(message, cause);
    }
}
