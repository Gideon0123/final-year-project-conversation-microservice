package com.example.CONVERSATION_SERVICE.component;


import com.example.CONVERSATION_SERVICE.dto.PresenceTransition;
import com.example.CONVERSATION_SERVICE.service.MessageDeliveryService;
import com.example.CONVERSATION_SERVICE.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompSessionEventListener {

    private final UserPresenceService presenceService;
    private final MessageDeliveryService messageDeliveryService;

    @EventListener
    public void handleSessionConnected(
            SessionConnectedEvent event
    ) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(
                        event.getMessage()
                );

        if (accessor.getSessionId() == null
                || accessor.getUser() == null) {
            return;
        }

        Long userId =
                Long.parseLong(
                        accessor.getUser().getName()
                );

        PresenceTransition transition =
                presenceService.registerSession(
                        userId,
                        accessor.getSessionId()
                );

        if (transition.changed()) {

            messageDeliveryService
                    .publishPresenceUpdate(
                            transition.userId(),
                            true
                    );
        }
    }

    @EventListener
    public void handleSessionDisconnect(
            SessionDisconnectEvent event
    ) {

        PresenceTransition transition =
                presenceService.unregisterSession(
                        event.getSessionId()
                );

        if (transition.changed()
                && transition.userId() != null) {

            messageDeliveryService
                    .publishPresenceUpdate(
                            transition.userId(),
                            false
                    );
        }
    }
}