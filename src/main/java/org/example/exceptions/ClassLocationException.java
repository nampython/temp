package org.example.exceptions;

public class ClassLocationException extends RuntimeException {
    public ClassLocationException() {
    }

    public ClassLocationException(String message) {
        super(message);
    }

    public ClassLocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
