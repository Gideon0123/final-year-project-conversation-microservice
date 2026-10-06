package com.example.CONVERSATION_SERVICE.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationReadResult(
        Long conversationId,
        Long readerId,
        LocalDateTime readAt,
        int messagesRead,
        List<Long> messageIds
) {
}
