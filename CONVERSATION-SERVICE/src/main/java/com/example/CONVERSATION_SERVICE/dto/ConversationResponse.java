package com.example.CONVERSATION_SERVICE.dto;

import java.time.LocalDateTime;

public record ConversationResponse(

        Long id,
        Long participantOneId,
        Long participantTwoId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}