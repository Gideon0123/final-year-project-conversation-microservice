package com.example.CONVERSATION_SERVICE.service.impl;

import com.example.CONVERSATION_SERVICE.entity.Message;
import com.example.CONVERSATION_SERVICE.entity.MessageReceipt;
import com.example.CONVERSATION_SERVICE.repository.MessageReceiptRepository;
import com.example.CONVERSATION_SERVICE.service.MessageReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    "Recipient ID must be a valid positive value"
            );
        }

        Optional<MessageReceipt> existingReceipt =
                messageReceiptRepository
                        .findByMessageIdAndUserId(
                                message.getId(),
                                recipientId
                        );

        if (existingReceipt.isPresent()) {
            return existingReceipt.get();
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
}