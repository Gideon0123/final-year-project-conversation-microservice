package com.example.CONVERSATION_SERVICE.controller;

import com.example.CONVERSATION_SERVICE.dto.ApiResponse;
import com.example.CONVERSATION_SERVICE.dto.ConversationResponse;
import com.example.CONVERSATION_SERVICE.dto.CreateConversationRequest;
import com.example.CONVERSATION_SERVICE.service.ConversationService;

import com.example.CONVERSATION_SERVICE.service.MessageReceiptService;
import com.example.CONVERSATION_SERVICE.utils.TraceIdUtil;
import jakarta.servlet.http.HttpServletRequest;
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
    private final MessageReceiptService messageReceiptService;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @Valid @RequestBody CreateConversationRequest request,
            HttpServletRequest httpRequest,
            Principal principal
    ) {
        Long currentUserId = extractUserId(principal);

        ConversationResponse response = conversationService.createConversation(
                currentUserId,
                request.participantId()
        );

        return ResponseEntity.ok(
                ApiResponse.<ConversationResponse>builder()
                        .success(true)
                        .message("Conversation created successfully")
                        .status(HttpStatus.CREATED.value())
                        .data(response)
                        .errors(null)
                        .path(httpRequest.getRequestURI())
                        .traceId(TraceIdUtil.generate())
                        .timestamp(java.time.LocalDateTime.now())
                        .build()
        );
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
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @PathVariable Long conversationId,
            HttpServletRequest request,
            Principal principal
    ) {
        Long currentUserId = extractUserId(principal);

        ConversationResponse response = conversationService.getConversationForUser(
                conversationId,
                currentUserId
        );

        return ResponseEntity.ok(
                ApiResponse.<ConversationResponse>builder()
                        .success(true)
                        .message("Conversation Fetched successfully")
                        .status(HttpStatus.OK.value())
                        .data(response)
                        .errors(null)
                        .path(request.getRequestURI())
                        .traceId(TraceIdUtil.generate())
                        .timestamp(java.time.LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            Principal principal,
            HttpServletRequest request
    ) {
        Long userId = Long.parseLong(principal.getName());
        long unread = messageReceiptService.getUnreadCount(userId);

        return ResponseEntity.ok(
                ApiResponse.<Long>builder()
                        .success(true)
                        .message("Unread Count Fetched successfully")
                        .status(HttpStatus.OK.value())
                        .data(unread)
                        .errors(null)
                        .path(request.getRequestURI())
                        .traceId(TraceIdUtil.generate())
                        .timestamp(java.time.LocalDateTime.now())
                        .build()
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