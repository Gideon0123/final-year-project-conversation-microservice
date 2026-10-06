package com.example.CONVERSATION_SERVICE.controller;

import com.example.CONVERSATION_SERVICE.dto.ConversationResponse;
import com.example.CONVERSATION_SERVICE.dto.CreateConversationRequest;
import com.example.CONVERSATION_SERVICE.service.ConversationService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ConversationResponse> createConversation(
            @Valid @RequestBody CreateConversationRequest request,
            Principal principal
    ) {
        Long currentUserId = extractUserId(principal);

        ConversationResponse response =
                conversationService.createConversation(
                        currentUserId,
                        request.participantId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getMyConversations(
            Principal principal
    ) {
        Long currentUserId = extractUserId(principal);

        List<ConversationResponse> response = conversationService.getUserConversations(
                currentUserId
        );

        return ResponseEntity.ok(
                response
        );
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable Long conversationId,
            Principal principal
    ) {
        Long currentUserId = extractUserId(principal);

        ConversationResponse response = conversationService.getConversationForUser(
                conversationId,
                currentUserId
        );

        return ResponseEntity.ok(
                response
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

        return Long.parseLong(
                principal.getName()
        );
    }
}