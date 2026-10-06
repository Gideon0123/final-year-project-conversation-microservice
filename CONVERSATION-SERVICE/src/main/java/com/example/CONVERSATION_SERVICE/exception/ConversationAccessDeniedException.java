package com.example.CONVERSATION_SERVICE.exception;

public class ConversationAccessDeniedException extends RuntimeException {

    public ConversationAccessDeniedException(String message) {
        super(message);
    }
}