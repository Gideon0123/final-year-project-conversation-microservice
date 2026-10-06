package com.example.CONVERSATION_SERVICE.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String content,
        LocalDateTime createdAt,

        Long recipientId,
        LocalDateTime deliveredAt,
        LocalDateTime readAt
) {

    public MessageResponse withReceipt(
            Long recipientId,
            LocalDateTime deliveredAt,
            LocalDateTime readAt
    ) {
        return new MessageResponse(
                id,
                conversationId,
                senderId,
                content,
                createdAt,
                recipientId,
                deliveredAt,
                readAt
        );
    }
}