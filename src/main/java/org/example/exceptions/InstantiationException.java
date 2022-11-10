package org.example.exceptions;

public class InstantiationException extends RuntimeException {
    public InstantiationException(String message) {
        super(message);
    }

    public InstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}
