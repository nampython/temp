package org.example.exceptions;

public class PostConstructException extends RuntimeException{
    public PostConstructException(String message) {
        super(message);
    }

    public PostConstructException(String message, Throwable cause) {
        super(message, cause);
    }
}
