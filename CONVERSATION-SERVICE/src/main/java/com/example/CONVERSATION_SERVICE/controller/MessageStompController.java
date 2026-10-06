package com.example.CONVERSATION_SERVICE.controller;

import com.example.CONVERSATION_SERVICE.dto.*;
import com.example.CONVERSATION_SERVICE.entity.MessageReceipt;
import com.example.CONVERSATION_SERVICE.service.ConversationService;
import com.example.CONVERSATION_SERVICE.service.MessageDeliveryService;
import com.example.CONVERSATION_SERVICE.service.MessageReceiptService;
import com.example.CONVERSATION_SERVICE.service.MessageService;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MessageStompController {

    private final MessageService messageService;
    private final MessageReceiptService messageReceiptService;
    private final ConversationService conversationService;
    private final MessageDeliveryService messageDeliveryService;

    @MessageMapping(
            "/conversations/{conversationId}/messages"
    )
    public void sendMessage(
            @DestinationVariable Long conversationId,
            SendMessageRequest request,
            Principal principal
    ) {

        Long senderId = conversationService.getOtherParticipant(
                conversationId, Long.parseLong(principal.getName())
        );

        SendMessageResult result = messageService.sendMessage(
                conversationId,
                senderId,
                request.content()
        );

        /*
         * Persisted message already exists.
         *
         * Now attempt real-time delivery.
         *
         * If recipient is offline, nothing is pushed.
         * The message remains in MySQL with deliveredAt = null.
         */
        messageDeliveryService
                .publishLiveMessage(
                        result,
                        senderId
                );
    }

    @MessageMapping(
            "/conversations/{conversationId}/messages/{messageId}/delivered"
    )
    public void markDelivered(
            @DestinationVariable Long conversationId,
            @DestinationVariable Long messageId,
            Principal principal
    ) {

        Long userId =
                Long.parseLong(
                        principal.getName()
                );

        MessageReceipt receipt =
                messageReceiptService.markDelivered(
                        conversationId,
                        messageId,
                        userId
                );

        /*
         * Tell the sender that the recipient has actually
         * received the message.
         */
        messageDeliveryService
                .notifySenderOfStatus(
                        receipt
                );
    }

    @MessageMapping(
            "/conversations/{conversationId}/messages/{messageId}/read"
    )
    public void markRead(
            @DestinationVariable Long conversationId,
            @DestinationVariable Long messageId,
            Principal principal
    ) {

        Long userId =
                Long.parseLong(
                        principal.getName()
                );

        MessageReceipt receipt =
                messageReceiptService.markRead(
                        conversationId,
                        messageId,
                        userId
                );

        messageDeliveryService
                .notifySenderOfStatus(
                        receipt
                );
    }

    @MessageMapping(
            "/conversations/{conversationId}/read"
    )
    public void markConversationRead(
            @DestinationVariable Long conversationId,
            Principal principal
    ) {

        Long userId =
                Long.parseLong(
                        principal.getName()
                );

        ConversationReadResult result =
                messageReceiptService
                        .markConversationRead(
                                conversationId,
                                userId
                        );

        /*
         * We need the sender/counterpart to notify.
         * Because conversations are private 1-to-1, the sender
         * is the other participant.
         *
         * MessageReceiptService can return this directly later;
         * for now derive it from the first message if available.
         */
        Long senderId = conversationService.getOtherParticipant(
                conversationId,
                userId
        );

        if (senderId != null) {
            messageDeliveryService.notifySenderConversationRead(
                    result,
                    senderId
            );
        }
    }

    @MessageMapping("/conversations/sync")
    public void synchronize(
            Principal principal,
            SimpMessageHeaderAccessor accessor
    ) {

        Long userId =
                Long.parseLong(
                        principal.getName()
                );

        String sessionId =
                accessor.getSessionId();

        if (sessionId == null) {
            return;
        }

        int synchronizedMessages =
                messageDeliveryService
                        .synchronizeUndeliveredMessages(
                                userId,
                                sessionId
                        );

        messageDeliveryService.sendSyncResponse(
                userId,
                sessionId,
                new SyncResponse(
                        synchronizedMessages
                )
        );

        /*
         * Optional synchronization summary.
         */
        SyncResponse response = new SyncResponse(
                synchronizedMessages
        );


        /*
         * This can be sent to the specific reconnecting session.
         */
        // implement via messageDeliveryService if desired
    }
}