package com.example.CONVERSATION_SERVICE.controller;

import com.example.CONVERSATION_SERVICE.dto.*;
import com.example.CONVERSATION_SERVICE.entity.MessageReceipt;
import com.example.CONVERSATION_SERVICE.payload.PagedResponse;
import com.example.CONVERSATION_SERVICE.service.*;

import com.example.CONVERSATION_SERVICE.utils.TraceIdUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class MessageController {

    private final UserPresenceService presenceService;
    private final MessageService messageService;
    private final ConversationService conversationService;
    private final MessageReceiptService messageReceiptService;
    private final MessageDeliveryService messageDeliveryService;

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletRequest request,
            Principal principal
    ) {
        Long currentUserId = extractUserId(principal);
        int adjustedPage = Math.max(page - 1, 0);
        PagedResponse<MessageResponse> messages = messageService.getMessages(
                conversationId,
                currentUserId,
                adjustedPage, size, sortBy, sortDirection
        );

        PagedResponse<MessageResponse> response =
                PagedResponse.<MessageResponse>builder()
                        .content(messages.getContent())
                        .size(messages.getSize())
                        .page(messages.getPage())
                        .first(messages.isFirst())
                        .last(messages.isLast())
                        .totalElements(messages.getTotalElements())
                        .totalPages(messages.getTotalPages())
                        .build();

        return ResponseEntity.ok(
                ApiResponse.<PagedResponse<MessageResponse>>builder()
                        .success(true)
                        .message("Messages fetched successfully")
                        .status(HttpStatus.OK.value())
                        .data(response)
                        .errors(null)
                        .path(request.getRequestURI())
                        .traceId(TraceIdUtil.generate())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/{conversationId}/unread-count")
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

    @PatchMapping("/{conversationId}/messages/{messageId}/delivered")
    public ResponseEntity<?> markDelivered(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            Principal principal
    ) {
        Long userId = Long.parseLong(principal.getName());

        MessageReceipt receipt = messageReceiptService.markDelivered(
                conversationId,
                messageId,
                userId
        );

        messageDeliveryService.notifySenderOfStatus(receipt);

        return ResponseEntity.ok(
                new MessageStatusUpdate(
                        messageId,
                        conversationId,
                        userId,
                        receipt.getDeliveredAt(),
                        receipt.getReadAt()
                )
        );
    }

    @PatchMapping("/{conversationId}/messages/{messageId}/read")
    public ResponseEntity<?> markRead(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            Principal principal
    ) {
        Long userId = Long.parseLong(principal.getName());

        MessageReceipt receipt = messageReceiptService.markRead(
                conversationId,
                messageId,
                userId
        );

        messageDeliveryService.notifySenderOfStatus(receipt);

        return ResponseEntity.ok(
                new MessageStatusUpdate(
                        messageId,
                        conversationId,
                        userId,
                        receipt.getDeliveredAt(),
                        receipt.getReadAt()
                )
        );
    }

    @PatchMapping("/{conversationId}/read")
    public ResponseEntity<?> markConversationRead(
            @PathVariable Long conversationId,
            Principal principal
    ) {
        Long userId = Long.parseLong(principal.getName());

        conversationService.verifyParticipant(
                conversationId,
                userId
        );

        ConversationReadResult result = messageReceiptService.markConversationRead(
                conversationId,
                userId
        );

        Long otherUserId = conversationService.getOtherParticipant(
                conversationId,
                userId
        );

        messageDeliveryService.notifySenderConversationRead(
                result,
                otherUserId
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{conversationId}/presence")
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