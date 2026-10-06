package com.example.CONVERSATION_SERVICE.dto;

public record SendMessageResult(
        MessageResponse message,
        Long recipientId
) {
}