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
import com.example.CONVERSATION_SERVICE.service.MessageReceiptService;
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
    private final MessageReceiptService messageReceiptService;
    private final MessageMapper messageMapper;
    private final CollaborationClient collaborationClient;

    @Override
    @Transactional
    public SendMessageResult sendMessage(
            Long conversationId,
            Long senderId,
            String content
    ) {

        Conversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Conversation with ID "
                                                + conversationId
                                                + " not found"
                                )
                        );

        conversationService.verifyParticipant(
                conversationId,
                senderId
        );

        Long recipientId =
                determineRecipient(
                        conversation,
                        senderId
                );

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
                    "Collaboration Service call failed. status={}, url={}, body={}",
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
                            + " must be connected before messaging"
            );
        }

        Message message =
                Message.builder()
                        .conversation(conversation)
                        .senderId(senderId)
                        .content(content)
                        .build();

        Message savedMessage =
                messageRepository.save(message);

        /*
         * Create the recipient's persistent receipt.
         *
         * We intentionally do NOT mark it delivered yet.
         * Delivery will be handled in the next stage
         * when online presence is introduced.
         */
        messageReceiptService.createReceipt(
                savedMessage,
                recipientId
        );

        conversation.touch();

        MessageResponse response =
                messageMapper.toResponse(savedMessage);

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