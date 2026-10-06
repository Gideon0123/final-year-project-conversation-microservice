package com.example.CONVERSATION_SERVICE.dto;

public record PresenceTransition(
        Long userId,
        boolean changed
) {
}