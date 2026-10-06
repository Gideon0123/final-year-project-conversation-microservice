package com.example.CONVERSATION_SERVICE.service;

import com.example.CONVERSATION_SERVICE.entity.Message;
import com.example.CONVERSATION_SERVICE.entity.MessageReceipt;

import java.util.Optional;

public interface MessageReceiptService {

    MessageReceipt createReceipt(
            Message message,
            Long recipientId
    );

    Optional<MessageReceipt> findReceipt(
            Long messageId,
            Long userId
    );
}