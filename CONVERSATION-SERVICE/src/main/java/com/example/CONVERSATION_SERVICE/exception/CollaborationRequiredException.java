package com.example.CONVERSATION_SERVICE.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CollaborationRequiredException extends RuntimeException {

    public CollaborationRequiredException(String message) {
        super(message);
    }
}