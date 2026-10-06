package com.example.CONVERSATION_SERVICE.security;

import lombok.Builder;

import java.security.Principal;

@Builder
public record UserPrincipal(Long userId, String email, String role) implements Principal {

    @Override
    public String getName() {
        return userId.toString();
    }
}
