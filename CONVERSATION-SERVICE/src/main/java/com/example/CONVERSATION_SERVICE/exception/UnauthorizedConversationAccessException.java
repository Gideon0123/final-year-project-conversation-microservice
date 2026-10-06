package com.example.CONVERSATION_SERVICE.exception;

public class UnauthorizedConversationAccessException extends RuntimeException {
    public UnauthorizedConversationAccessException(String message) {
        super(message);
    }
}
