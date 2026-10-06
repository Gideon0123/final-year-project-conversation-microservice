package com.example.CONVERSATION_SERVICE.dto;

import java.time.LocalDateTime;

public record PresenceUpdate(
        Long userId,
        boolean online,
        LocalDateTime timestamp
) {
}