package com.example.CONVERSATION_SERVICE.service;

import com.example.CONVERSATION_SERVICE.dto.ConversationReadResult;
import com.example.CONVERSATION_SERVICE.dto.SendMessageResult;
import com.example.CONVERSATION_SERVICE.entity.MessageReceipt;

public interface MessageDeliveryService {

    void publishLiveMessage(
            SendMessageResult result,
            Long senderId
    );

    int synchronizeUndeliveredMessages(
            Long userId,
            String sessionId
    );

    void notifySenderOfStatus(
            MessageReceipt receipt
    );

    void notifySenderConversationRead(
            ConversationReadResult result,
            Long senderId
    );

    void publishPresenceUpdate(
            Long userId,
            boolean online
    );
}