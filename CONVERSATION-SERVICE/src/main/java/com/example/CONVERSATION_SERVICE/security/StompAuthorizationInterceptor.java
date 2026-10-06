package com.example.CONVERSATION_SERVICE.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class StompAuthorizationInterceptor
        implements ChannelInterceptor {

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        StompCommand command =
                accessor.getCommand();

        Principal principal =
                accessor.getUser();

        /*
         * CONNECT
         */
        if (StompCommand.CONNECT.equals(command)) {

            requireAuthenticated(principal);

            return message;
        }

        /*
         * DISCONNECT does not need additional
         * authorization checks.
         */
        if (StompCommand.DISCONNECT.equals(command)) {
            return message;
        }

        /*
         * Any other client operation must have
         * an authenticated Principal.
         */
        requireAuthenticated(principal);

        /*
         * MESSAGE / SEND
         */
        if (StompCommand.SEND.equals(command)) {

            String destination =
                    accessor.getDestination();

            if (destination == null
                    || !destination.startsWith("/app/")) {

                throw new MessagingException(
                        "Clients may only send messages "
                                + "to application destinations."
                );
            }
        }

        /*
         * SUBSCRIBE
         */
        if (StompCommand.SUBSCRIBE.equals(command)) {

            String destination =
                    accessor.getDestination();

            if (destination == null) {

                throw new MessagingException(
                        "Subscription destination is required."
                );
            }

            /*
             * For private messaging we want user
             * destinations.
             *
             * We can later add carefully controlled
             * /topic destinations if needed.
             */
            if (!destination.startsWith("/user/")) {

                throw new MessagingException(
                        "Clients may only subscribe "
                                + "to private user destinations."
                );
            }
        }

        return message;
    }

    private void requireAuthenticated(
            Principal principal
    ) {

        if (principal == null) {

            throw new MessagingException(
                    "Authentication is required."
            );
        }
    }
}