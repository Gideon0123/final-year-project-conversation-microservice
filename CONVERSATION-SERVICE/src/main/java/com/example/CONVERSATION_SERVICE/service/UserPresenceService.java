package com.example.CONVERSATION_SERVICE.service;

import com.example.CONVERSATION_SERVICE.dto.PresenceTransition;

import java.util.Set;

public interface UserPresenceService {

    PresenceTransition registerSession(
            Long userId,
            String sessionId
    );

    PresenceTransition unregisterSession(
            String sessionId
    );

    boolean isOnline(
            Long userId
    );

    int getActiveSessionCount(
            Long userId
    );

    Set<String> getSessionIds(
            Long userId
    );
}