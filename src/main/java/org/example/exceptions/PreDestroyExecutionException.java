package org.example.exceptions;

public class PreDestroyExecutionException extends InstantiationException{

    public PreDestroyExecutionException(String message) {
        super(message);
    }

    public PreDestroyExecutionException(String message, Throwable cause) {
        super(message, cause);
    }


}
