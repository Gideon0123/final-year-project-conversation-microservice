package com.example.CONVERSATION_SERVICE.dto;

import java.util.List;

public record ConversationPresenceResponse(
        Long conversationId,
        List<UserPresenceResponse> participants
) {
}