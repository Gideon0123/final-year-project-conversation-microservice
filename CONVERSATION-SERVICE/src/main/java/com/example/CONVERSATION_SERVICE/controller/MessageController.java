package com.example.CONVERSATION_SERVICE.controller;

import com.example.CONVERSATION_SERVICE.dto.MessageResponse;
import com.example.CONVERSATION_SERVICE.service.MessageService;

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

    private final MessageService messageService;

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

        Long currentUserId =
                extractUserId(principal);

        Page<MessageResponse> response =
                messageService.getMessages(
                        conversationId,
                        currentUserId,
                        pageable
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