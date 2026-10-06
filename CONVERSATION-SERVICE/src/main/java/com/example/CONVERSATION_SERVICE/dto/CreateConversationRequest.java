package com.example.CONVERSATION_SERVICE.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateConversationRequest(

        @NotNull(message = "Participant ID is required")
        @Positive(message = "Participant ID must be greater than zero")
        Long participantId
) {
}