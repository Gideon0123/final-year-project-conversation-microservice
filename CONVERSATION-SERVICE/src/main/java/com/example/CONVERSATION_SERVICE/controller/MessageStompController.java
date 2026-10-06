package com.example.CONVERSATION_SERVICE.controller;

import com.example.CONVERSATION_SERVICE.dto.MessageResponse;
import com.example.CONVERSATION_SERVICE.dto.SendMessageRequest;
import com.example.CONVERSATION_SERVICE.dto.SendMessageResult;
import com.example.CONVERSATION_SERVICE.dto.StompErrorResponse;
import com.example.CONVERSATION_SERVICE.exception.CollaborationRequiredException;
import com.example.CONVERSATION_SERVICE.exception.CollaborationServiceUnavailableException;
import com.example.CONVERSATION_SERVICE.exception.ConversationAccessDeniedException;
import com.example.CONVERSATION_SERVICE.exception.ResourceNotFoundException;
import com.example.CONVERSATION_SERVICE.service.MessageService;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class MessageStompController {

    private final MessageService messageService;

    private final SimpMessagingTemplate
            messagingTemplate;

    @MessageMapping(
            "/conversations/{conversationId}/messages"
    )
    public void sendMessage(
            @DestinationVariable("conversationId")
            Long conversationId,

            SendMessageRequest request,

            Principal principal
    ) {

        if (principal == null) {

            throw new ConversationAccessDeniedException(
                    "Authenticated user is required"
            );
        }

        Long senderId =
                extractUserId(principal);

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "       STOMP MESSAGE RECEIVED"
        );
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "Conversation ID: "
                        + conversationId
        );

        System.out.println(
                "Sender ID: "
                        + senderId
        );

        System.out.println(
                "Content: "
                        + request.content()
        );

        System.out.println(
                "=========================================="
        );
        System.out.println();

        /*
         * This is the authoritative business operation.
         *
         * If the conversation doesn't exist,
         * ResourceNotFoundException is thrown.
         *
         * If the user isn't a participant,
         * ConversationAccessDeniedException is thrown.
         */
        SendMessageResult result =
                messageService.sendMessage(
                        conversationId,
                        senderId,
                        request.content()
                );

        MessageResponse response = result.message();

        Long recipientId = result.recipientId();

        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/messages",
                response
        );

        messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/queue/messages",
                response
        );
    }

    @MessageExceptionHandler(
            ResourceNotFoundException.class
    )
    public void handleResourceNotFound(
            ResourceNotFoundException exception,
            Principal principal
    ) {

        sendError(
                principal,
                "CONVERSATION_NOT_FOUND",
                exception.getMessage()
        );
    }

    @MessageExceptionHandler(
            ConversationAccessDeniedException.class
    )
    public void handleConversationAccessDenied(
            ConversationAccessDeniedException exception,
            Principal principal
    ) {

        sendError(
                principal,
                "CONVERSATION_ACCESS_DENIED",
                exception.getMessage()
        );
    }

    @MessageExceptionHandler(
            CollaborationRequiredException.class
    )
    public void handleCollaborationRequired(
            CollaborationRequiredException exception,
            Principal principal
    ) {

        sendError(
                principal,
                "COLLABORATION_REQUIRED",
                exception.getMessage()
        );
    }

    @MessageExceptionHandler(
            CollaborationServiceUnavailableException.class
    )
    public void handleCollaborationServiceUnavailable(
            CollaborationServiceUnavailableException exception,
            Principal principal
    ) {

        sendError(
                principal,
                "COLLABORATION_SERVICE_UNAVAILABLE",
                "Unable to verify your collaboration relationship right now."
        );
    }

    @MessageExceptionHandler(
            Exception.class
    )
    public void handleUnexpectedException(
            Exception exception,
            Principal principal
    ) {

        sendError(
                principal,
                "MESSAGE_SEND_FAILED",
                "Unable to process the message."
        );

        /*
         * Keep the actual exception in server logs.
         * Do NOT expose internal exception details
         * to the client.
         */
        exception.printStackTrace();
    }

    private void sendError(
            Principal principal,
            String code,
            String message
    ) {

        if (principal == null) {
            return;
        }

        StompErrorResponse response =
                new StompErrorResponse(
                        code,
                        message,
                        LocalDateTime.now()
                );

        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                response
        );
    }

    private Long extractUserId(
            Principal principal
    ) {

        return Long.parseLong(
                principal.getName()
        );
    }
}