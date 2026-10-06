package com.example.CONVERSATION_SERVICE.dto;

import java.time.LocalDateTime;

public record MessageReceiptResponse(
        Long id,
        Long messageId,
        Long userId,
        LocalDateTime deliveredAt,
        LocalDateTime readAt
) {
}
