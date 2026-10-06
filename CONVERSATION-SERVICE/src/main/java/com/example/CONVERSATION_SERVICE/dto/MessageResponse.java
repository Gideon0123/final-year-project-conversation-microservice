package com.example.CONVERSATION_SERVICE.dto;

import java.time.LocalDateTime;

public record MessageResponse(

        Long id,
        Long conversationId,
        Long senderId,
        String content,
        LocalDateTime createdAt
) {
}