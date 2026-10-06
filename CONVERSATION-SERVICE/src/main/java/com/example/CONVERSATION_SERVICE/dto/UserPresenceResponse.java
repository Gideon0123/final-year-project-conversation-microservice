package com.example.CONVERSATION_SERVICE.dto;

public record UserPresenceResponse(
        Long userId,
        boolean online,
        int activeSessions
) {
}