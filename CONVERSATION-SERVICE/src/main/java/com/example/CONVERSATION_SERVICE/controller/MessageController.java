package com.example.CONVERSATION_SERVICE.controller;

import com.example.CONVERSATION_SERVICE.dto.ConversationPresenceResponse;
import com.example.CONVERSATION_SERVICE.dto.ConversationResponse;
import com.example.CONVERSATION_SERVICE.dto.MessageResponse;
import com.example.CONVERSATION_SERVICE.dto.UserPresenceResponse;
import com.example.CONVERSATION_SERVICE.service.ConversationService;
import com.example.CONVERSATION_SERVICE.service.MessageReceiptService;
import com.example.CONVERSATION_SERVICE.service.MessageService;

import com.example.CONVERSATION_SERVICE.service.UserPresenceService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/conversations/{conversationId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final UserPresenceService presenceService;
    private final MessageService messageService;
    private final ConversationService conversationService;
    private final MessageReceiptService messageReceiptService;

    @GetMapping
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable Long conversationId,

            @PageableDefault(
                    size = 30,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            Principal principal
    ) {

        Long currentUserId = extractUserId(principal);

        Page<MessageResponse> response = messageService.getMessages(
                conversationId,
                currentUserId,
                pageable
        );

        return ResponseEntity.ok(
                response
        );
    }

    @GetMapping(
            "/conversations/{conversationId}/unread-count"
    )
    public ResponseEntity<?> getConversationUnreadCount(
            @PathVariable Long conversationId,
            Principal principal
    ) {
        Long userId = Long.parseLong(principal.getName());

        /*
         * Your ConversationService should validate that the user
         * belongs to the conversation before exposing its count.
         */
        conversationService.verifyParticipant(
                conversationId,
                userId
        );

        long unread = messageReceiptService.getUnreadCount(
                conversationId,
                userId
        );

        return ResponseEntity.ok(unread);
    }

    @GetMapping(
            "/conversations/{conversationId}/presence"
    )
    public ResponseEntity<?> getPresence(
            @PathVariable Long conversationId,
            Principal principal
    ) {
        Long currentUserId = Long.parseLong(principal.getName());

        ConversationResponse conversation = conversationService.getConversationForUser(
                conversationId,
                currentUserId
        );

        UserPresenceResponse first = new UserPresenceResponse(
                conversation.participantOneId(),
                presenceService.isOnline(conversation.participantOneId()),
                presenceService.getActiveSessionCount(conversation.participantOneId())
        );

        UserPresenceResponse second = new UserPresenceResponse(
                conversation.participantTwoId(),
                presenceService.isOnline(conversation.participantTwoId()),
                presenceService.getActiveSessionCount(conversation.participantTwoId())
        );

        return ResponseEntity.ok(
                new ConversationPresenceResponse(
                        conversationId, java.util.List.of(first, second)
                )
        );
    }

    private Long extractUserId(
            Principal principal
    ) {
        if (principal == null) {

            throw new IllegalStateException(
                    "Authenticated principal is required"
            );
        }

        return Long.parseLong(principal.getName());
    }
}