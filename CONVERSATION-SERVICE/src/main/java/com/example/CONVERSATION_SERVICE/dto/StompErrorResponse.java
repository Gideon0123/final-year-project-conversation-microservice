package com.example.CONVERSATION_SERVICE.dto;

import java.time.LocalDateTime;

public record StompErrorResponse(

        String code,

        String message,

        LocalDateTime timestamp
) {
}