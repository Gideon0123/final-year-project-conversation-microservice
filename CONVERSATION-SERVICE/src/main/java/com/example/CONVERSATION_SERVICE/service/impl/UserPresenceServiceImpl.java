package com.example.CONVERSATION_SERVICE.service.impl;

import com.example.CONVERSATION_SERVICE.dto.PresenceTransition;
import com.example.CONVERSATION_SERVICE.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserPresenceServiceImpl implements UserPresenceService {

    /*
     * userId -> active websocket session IDs
     */
    private final ConcurrentHashMap<Long, Set<String>>
            userSessions =
            new ConcurrentHashMap<>();

    /*
     * sessionId -> userId
     *
     * This allows a disconnect event to identify the
     * user without trusting a duplicate event's principal.
     */
    private final ConcurrentHashMap<String, Long>
            sessionUsers =
            new ConcurrentHashMap<>();

    @Override
    public PresenceTransition registerSession(
            Long userId,
            String sessionId
    ) {

        sessionUsers.put(
                sessionId,
                userId
        );

        Set<String> sessions =
                userSessions.computeIfAbsent(
                        userId,
                        ignored ->
                                ConcurrentHashMap.newKeySet()
                );

        boolean changed =
                sessions.isEmpty();

        sessions.add(sessionId);

        return new PresenceTransition(
                userId,
                changed
        );
    }

    @Override
    public PresenceTransition unregisterSession(
            String sessionId
    ) {

        Long userId =
                sessionUsers.remove(sessionId);

        if (userId == null) {
            return new PresenceTransition(
                    null,
                    false
            );
        }

        Set<String> sessions =
                userSessions.get(userId);

        if (sessions == null) {
            return new PresenceTransition(
                    userId,
                    false
            );
        }

        sessions.remove(sessionId);

        if (!sessions.isEmpty()) {
            return new PresenceTransition(
                    userId,
                    false
            );
        }

        userSessions.remove(
                userId,
                sessions
        );

        return new PresenceTransition(
                userId,
                true
        );
    }

    @Override
    public boolean isOnline(
            Long userId
    ) {

        Set<String> sessions =
                userSessions.get(userId);

        return sessions != null
                && !sessions.isEmpty();
    }

    @Override
    public int getActiveSessionCount(
            Long userId
    ) {

        Set<String> sessions =
                userSessions.get(userId);

        return sessions == null
                ? 0
                : sessions.size();
    }

    @Override
    public Set<String> getSessionIds(
            Long userId
    ) {

        Set<String> sessions =
                userSessions.get(userId);

        if (sessions == null) {
            return Collections.emptySet();
        }

        return Set.copyOf(sessions);
    }
}