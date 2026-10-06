package com.example.CONVERSATION_SERVICE.security;

import java.security.Principal;
import java.time.Instant;

public record AuthenticatedUser(
        Long userId,
        String email,
        String role,
        Instant expiresAt
) implements Principal {

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
