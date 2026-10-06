package com.example.CONVERSATION_SERVICE.exception;

public class CollaborationServiceUnavailableException
        extends RuntimeException {

    public CollaborationServiceUnavailableException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}