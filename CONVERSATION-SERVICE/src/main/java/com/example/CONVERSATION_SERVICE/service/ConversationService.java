package com.example.CONVERSATION_SERVICE.service;

import com.example.CONVERSATION_SERVICE.dto.ConversationResponse;

import java.util.List;

public interface ConversationService {

    ConversationResponse createConversation(
            Long currentUserId,
            Long otherParticipantId
    );

    ConversationResponse getConversationForUser(
            Long conversationId,
            Long currentUserId
    );

    List<ConversationResponse> getUserConversations(
            Long currentUserId
    );

    boolean isParticipant(
            Long conversationId,
            Long userId
    );

    void verifyParticipant(
            Long conversationId,
            Long userId
    );
}