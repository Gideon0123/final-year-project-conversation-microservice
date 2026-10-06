package com.example.CONVERSATION_SERVICE.service.impl;

import com.example.CONVERSATION_SERVICE.dto.ConversationReadResult;
import com.example.CONVERSATION_SERVICE.entity.Message;
import com.example.CONVERSATION_SERVICE.entity.MessageReceipt;
import com.example.CONVERSATION_SERVICE.exception.ResourceNotFoundException;
import com.example.CONVERSATION_SERVICE.repository.ConversationUnreadCountProjection;
import com.example.CONVERSATION_SERVICE.repository.MessageReceiptRepository;
import com.example.CONVERSATION_SERVICE.service.MessageReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageReceiptServiceImpl
        implements MessageReceiptService {

    private final MessageReceiptRepository messageReceiptRepository;

    @Override
    public MessageReceipt createReceipt(
            Message message,
            Long recipientId
    ) {

        if (message == null) {
            throw new IllegalArgumentException(
                    "Message must not be null"
            );
        }

        if (recipientId == null || recipientId <= 0) {
            throw new IllegalArgumentException(
                    "Recipient ID must be valid"
            );
        }

        Optional<MessageReceipt> existing =
                messageReceiptRepository
                        .findByMessageIdAndUserId(
                                message.getId(),
                                recipientId
                        );

        if (existing.isPresent()) {
            return existing.get();
        }

        MessageReceipt receipt =
                MessageReceipt.builder()
                        .message(message)
                        .userId(recipientId)
                        .build();

        return messageReceiptRepository.save(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MessageReceipt> findReceipt(
            Long messageId,
            Long userId
    ) {
        return messageReceiptRepository
                .findByMessageIdAndUserId(
                        messageId,
                        userId
                );
    }

    @Override
    public MessageReceipt markDelivered(
            Long conversationId,
            Long messageId,
            Long userId
    ) {

        MessageReceipt receipt =
                messageReceiptRepository
                        .findByMessageIdAndUserId(
                                messageId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Message receipt not found"
                                )
                        );

        Message message = receipt.getMessage();

        if (!message.getConversation()
                .getId()
                .equals(conversationId)) {

            throw new ResourceNotFoundException(
                    "Message does not belong to this conversation"
            );
        }

        receipt.markDelivered();

        return messageReceiptRepository.save(receipt);
    }

    @Override
    public MessageReceipt markRead(
            Long conversationId,
            Long messageId,
            Long userId
    ) {

        MessageReceipt receipt =
                messageReceiptRepository
                        .findByMessageIdAndUserId(
                                messageId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Message receipt not found"
                                )
                        );

        Message message = receipt.getMessage();

        if (!message.getConversation()
                .getId()
                .equals(conversationId)) {

            throw new ResourceNotFoundException(
                    "Message does not belong to this conversation"
            );
        }

        /*
         * Reading a message necessarily means it has
         * been delivered.
         */
        receipt.markRead();

        return messageReceiptRepository.save(receipt);
    }

    @Override
    public ConversationReadResult markConversationRead(
            Long conversationId,
            Long userId
    ) {

        List<MessageReceipt> unread =
                messageReceiptRepository
                        .findUnreadMessages(
                                conversationId,
                                userId
                        );

        LocalDateTime readAt =
                LocalDateTime.now();

        for (MessageReceipt receipt : unread) {
            receipt.markDelivered();

            if (receipt.getReadAt() == null) {
                receipt.setReadAt(readAt);
            }
        }

        messageReceiptRepository.saveAll(unread);

        List<Long> messageIds =
                unread.stream()
                        .map(receipt ->
                                receipt.getMessage().getId())
                        .toList();

        return new ConversationReadResult(
                conversationId,
                userId,
                readAt,
                messageIds.size(),
                messageIds
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(
            Long userId
    ) {
        return messageReceiptRepository
                .countUnreadByUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(
            Long conversationId,
            Long userId
    ) {
        return messageReceiptRepository
                .countUnreadByConversation(
                        conversationId,
                        userId
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getUnreadCountsByConversation(
            Long userId
    ) {

        List<ConversationUnreadCountProjection>
                projections =
                messageReceiptRepository
                        .findUnreadCountsByUser(userId);

        Map<Long, Long> result = new HashMap<>();

        for (ConversationUnreadCountProjection projection
                : projections) {

            result.put(
                    projection.getConversationId(),
                    projection.getUnreadCount()
            );
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageReceipt> getUndeliveredMessages(
            Long userId
    ) {
        return messageReceiptRepository
                .findUndeliveredMessages(userId);
    }
}