package com.example.CONVERSATION_SERVICE.service.impl;

import com.example.CONVERSATION_SERVICE.dto.ConversationResponse;
import com.example.CONVERSATION_SERVICE.entity.Conversation;
import com.example.CONVERSATION_SERVICE.exception.CollaborationRequiredException;
import com.example.CONVERSATION_SERVICE.exception.ConversationAccessDeniedException;
import com.example.CONVERSATION_SERVICE.exception.ResourceNotFoundException;
import com.example.CONVERSATION_SERVICE.exception.UnauthorizedConversationAccessException;
import com.example.CONVERSATION_SERVICE.feign.CollaborationClient;
import com.example.CONVERSATION_SERVICE.mapper.ConversationMapper;
import com.example.CONVERSATION_SERVICE.repository.ConversationRepository;
import com.example.CONVERSATION_SERVICE.service.ConversationService;

import com.example.CONVERSATION_SERVICE.service.MessageReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;
    private final CollaborationClient collaborationClient;
    private final MessageReceiptService messageReceiptService;

    @Override
    @Transactional
    public ConversationResponse createConversation(
            Long currentUserId,
            Long otherParticipantId
    ) {
        validateDifferentUsers(
                currentUserId,
                otherParticipantId
        );

        boolean connected =
                collaborationClient.areConnected(
                        currentUserId,
                        otherParticipantId
                );

        if (!connected) {

            throw new CollaborationRequiredException(
                    "You can only start a conversation "
                            + "with a connected researcher."
            );
        }

        Long participantOneId =
                Math.min(
                        currentUserId,
                        otherParticipantId
                );

        Long participantTwoId =
                Math.max(
                        currentUserId,
                        otherParticipantId
                );

        Conversation conversation =
                conversationRepository
                        .findByParticipantOneIdAndParticipantTwoId(
                                participantOneId,
                                participantTwoId
                        )
                        .orElseGet(() ->
                                conversationRepository.save(
                                        Conversation.builder()
                                                .participantOneId(
                                                        participantOneId
                                                )
                                                .participantTwoId(
                                                        participantTwoId
                                                )
                                                .build()
                                )
                        );

        return conversationMapper.toResponse(conversation)
                .withUnreadCount(0);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversationForUser(
            Long conversationId,
            Long currentUserId
    ) {

        Conversation conversation =
                getConversation(conversationId);

        verifyParticipant(
                conversation,
                currentUserId
        );

        ConversationResponse response = conversationMapper.toResponse(conversation);

        long unreadCount = messageReceiptService.getUnreadCount(
                conversationId,
                currentUserId
        );

        return response.withUnreadCount(
                unreadCount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Long getOtherParticipant(
            Long conversationId,
            Long currentUserId
    ) {
        Conversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Conversation with ID "
                                                + conversationId
                                                + " not found"
                                )
                        );

        verifyParticipant(
                conversation,
                currentUserId
        );

        if (conversation.getParticipantOneId().equals(currentUserId)) {

            return conversation.getParticipantTwoId();
        }

        return conversation.getParticipantOneId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(
            Long currentUserId
    ) {

        List<Conversation> conversations = conversationRepository.findUserConversations(
                currentUserId
        );

        Map<Long, Long> unreadCounts = messageReceiptService.getUnreadCountsByConversation(
                currentUserId
        );

        return conversations.stream()
                .map(conversation -> {

                    long unread =
                            unreadCounts.getOrDefault(
                                    conversation.getId(),
                                    0L
                            );

                    return conversationMapper
                            .toResponse(conversation)
                            .withUnreadCount(unread);

                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isParticipant(
            Long conversationId,
            Long userId
    ) {

        Conversation conversation =
                getConversation(conversationId);

        return isParticipant(
                conversation,
                userId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public void verifyParticipant(
            Long conversationId,
            Long userId
    ) {

        Conversation conversation =
                getConversation(conversationId);

        verifyParticipant(
                conversation,
                userId
        );
    }

    private Conversation getConversation(
            Long conversationId
    ) {

        return conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conversation with ID "
                                        + conversationId
                                        + " not found"
                        )
                );
    }

    private void verifyParticipant(
            Conversation conversation,
            Long userId
    ) {

        if (!isParticipant(
                conversation,
                userId
        )) {

            throw new ConversationAccessDeniedException(
                    "User " + userId
                            + " is not a participant of conversation "
                            + conversation.getId()
            );
        }
    }

    private boolean isParticipant(
            Conversation conversation,
            Long userId
    ) {

        return conversation
                .getParticipantOneId()
                .equals(userId)
                || conversation
                .getParticipantTwoId()
                .equals(userId);
    }

    private void validateDifferentUsers(
            Long currentUserId,
            Long otherParticipantId
    ) {

        if (currentUserId.equals(
                otherParticipantId
        )) {

            throw new UnauthorizedConversationAccessException(
                    "You cannot create a conversation with yourself"
            );
        }
    }
}