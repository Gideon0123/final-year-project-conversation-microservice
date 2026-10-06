package com.example.CONVERSATION_SERVICE.service;

import com.example.CONVERSATION_SERVICE.entity.Message;
import com.example.CONVERSATION_SERVICE.entity.MessageReceipt;
import com.example.CONVERSATION_SERVICE.dto.ConversationReadResult;

import java.util.List;
import java.util.Map;
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

    MessageReceipt markDelivered(
            Long conversationId,
            Long messageId,
            Long userId
    );

    MessageReceipt markRead(
            Long conversationId,
            Long messageId,
            Long userId
    );

    ConversationReadResult markConversationRead(
            Long conversationId,
            Long userId
    );

    long getUnreadCount(
            Long userId
    );

    long getUnreadCount(
            Long conversationId,
            Long userId
    );

    Map<Long, Long> getUnreadCountsByConversation(
            Long userId
    );

    MessageReceipt findReceiptForStatusNotification(
            Long messageId
    );

    List<MessageReceipt> getUndeliveredMessages(
            Long userId
    );
}