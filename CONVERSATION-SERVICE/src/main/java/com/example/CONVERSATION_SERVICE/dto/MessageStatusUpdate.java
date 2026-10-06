package com.example.CONVERSATION_SERVICE.dto;

import java.time.LocalDateTime;

public record MessageStatusUpdate(
        Long messageId,
        Long conversationId,
        Long recipientId,
        LocalDateTime deliveredAt,
        LocalDateTime readAt
) {
}
