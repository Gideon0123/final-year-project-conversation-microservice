package com.example.CONVERSATION_SERVICE.service.impl;

import com.example.CONVERSATION_SERVICE.dto.MessageResponse;
import com.example.CONVERSATION_SERVICE.dto.SendMessageResult;
import com.example.CONVERSATION_SERVICE.entity.Conversation;
import com.example.CONVERSATION_SERVICE.entity.Message;
import com.example.CONVERSATION_SERVICE.exception.CollaborationRequiredException;
import com.example.CONVERSATION_SERVICE.exception.CollaborationServiceUnavailableException;
import com.example.CONVERSATION_SERVICE.exception.ResourceNotFoundException;
import com.example.CONVERSATION_SERVICE.feign.CollaborationClient;
import com.example.CONVERSATION_SERVICE.mapper.MessageMapper;
import com.example.CONVERSATION_SERVICE.repository.ConversationRepository;
import com.example.CONVERSATION_SERVICE.repository.MessageRepository;
import com.example.CONVERSATION_SERVICE.service.ConversationService;
import com.example.CONVERSATION_SERVICE.service.MessageService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final MessageMapper messageMapper;
    private final CollaborationClient collaborationClient;

    @Override
    @Transactional
    public SendMessageResult sendMessage(
            Long conversationId,
            Long senderId,
            String content
    ) {

        /*
         * 1. Conversation must exist.
         */
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

        /*
         * 2. Authenticated sender must be a
         *    participant.
         */
        conversationService.verifyParticipant(
                conversationId,
                senderId
        );

        /*
         * 3. Determine the other participant.
         */
        Long recipientId =
                determineRecipient(
                        conversation,
                        senderId
                );

        /*
         * 4. Verify collaboration relationship.
         */
        boolean connected;

        try {

            connected =
                    collaborationClient.areConnected(
                            senderId,
                            recipientId
                    );

            log.info(
                    "Collaboration check: sender={}, recipient={}, connected={}",
                    senderId,
                    recipientId,
                    connected
            );

        } catch (FeignException ex) {

            log.error(
                    "Collaboration Service call failed. " +
                            "status={}, url={}, body={}",
                    ex.status(),
                    ex.request() != null
                            ? ex.request().url()
                            : "unknown",
                    ex.contentUTF8(),
                    ex
            );

            throw new CollaborationServiceUnavailableException(
                    "Unable to verify the collaboration relationship at this time",
                    ex
            );
        }

        if (!connected) {

            throw new CollaborationRequiredException(
                    "Users "
                            + senderId
                            + " and "
                            + recipientId
                            + " do not have "
                            + "an active collaboration connection"
            );
        }

        /*
         * 5. Save only after every authorization
         *    check has passed.
         */
        Message message =
                Message.builder()
                        .conversation(conversation)
                        .senderId(senderId)
                        .content(content)
                        .build();

        Message savedMessage = messageRepository.save(
                message
        );

        conversation.touch();

        MessageResponse response =
                messageMapper.toResponse(
                        savedMessage
                );

        return new SendMessageResult(
                response,
                recipientId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(
            Long conversationId,
            Long currentUserId,
            Pageable pageable
    ) {

        conversationService.verifyParticipant(
                conversationId,
                currentUserId
        );

        return messageRepository
                .findByConversationId(
                        conversationId,
                        pageable
                )
                .map(messageMapper::toResponse);
    }

    private Long determineRecipient(
            Conversation conversation,
            Long senderId
    ) {

        if (conversation
                .getParticipantOneId()
                .equals(senderId)) {

            return conversation
                    .getParticipantTwoId();
        }

        return conversation
                .getParticipantOneId();
    }
}