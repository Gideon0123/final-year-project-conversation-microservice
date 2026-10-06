package com.example.CONVERSATION_SERVICE.service.impl;

import com.example.CONVERSATION_SERVICE.dto.*;
import com.example.CONVERSATION_SERVICE.entity.Conversation;
import com.example.CONVERSATION_SERVICE.entity.Message;
import com.example.CONVERSATION_SERVICE.entity.MessageReceipt;
import com.example.CONVERSATION_SERVICE.mapper.MessageMapper;
import com.example.CONVERSATION_SERVICE.repository.ConversationRepository;
import com.example.CONVERSATION_SERVICE.service.MessageDeliveryService;
import com.example.CONVERSATION_SERVICE.service.MessageReceiptService;
import com.example.CONVERSATION_SERVICE.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageDeliveryServiceImpl implements MessageDeliveryService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserPresenceService presenceService;
    private final MessageReceiptService messageReceiptService;
    private final MessageMapper messageMapper;
    private final ConversationRepository conversationRepository;

    @Override
    public void publishLiveMessage(
            SendMessageResult result,
            Long senderId
    ) {

        Long recipientId =
                result.recipientId();

        MessageResponse message =
                result.message();

        /*
         * Always send the message back to the sender.
         */
        sendToUser(
                senderId,
                "/queue/messages",
                message
        );

        /*
         * Only attempt real-time delivery when the
         * recipient is currently online.
         *
         * IMPORTANT:
         * online != delivered.
         *
         * The client must still send the DELIVERED
         * acknowledgement.
         */
        if (presenceService.isOnline(recipientId)) {

            sendToUser(
                    recipientId,
                    "/queue/messages",
                    message
            );
        }
    }

    @Override
    public int synchronizeUndeliveredMessages(
            Long userId,
            String sessionId
    ) {

        var undelivered =
                messageReceiptService
                        .getUndeliveredMessages(userId);

        int sent = 0;

        for (MessageReceipt receipt : undelivered) {

            Message message =
                    receipt.getMessage();

            MessageResponse response =
                    messageMapper
                            .toResponse(message)
                            .withReceipt(
                                    receipt.getUserId(),
                                    receipt.getDeliveredAt(),
                                    receipt.getReadAt()
                            );

            try {

                sendToUserSession(
                        userId,
                        sessionId,
                        "/queue/messages",
                        response
                );

                sent++;

            } catch (MessageDeliveryException ex) {

                /*
                 * DO NOT mark delivered.
                 *
                 * The database receipt stays undelivered,
                 * so the next synchronization can retry it.
                 */
                log.warn(
                        "Could not synchronize message {} to user {} session {}",
                        message.getId(),
                        userId,
                        sessionId,
                        ex
                );

                break;
            }
        }

        return sent;
    }

//    @Override
//    public void publishDeliveredUpdate(
//            Long messageId
//    ) {
//
//        MessageReceipt receipt =
//                messageReceiptService
//                        .findReceiptForStatusNotification(
//                                messageId
//                        );
//
//        if (receipt == null) {
//            return;
//        }
//
//        Message message =
//                receipt.getMessage();
//
//        MessageStatusUpdate update =
//                new MessageStatusUpdate(
//                        message.getId(),
//                        message.getConversation().getId(),
//                        receipt.getUserId(),
//                        receipt.getDeliveredAt(),
//                        receipt.getReadAt()
//                );
//
//        /*
//         * Notify the original sender.
//         */
//        sendToUser(
//                message.getSenderId(),
//                "/queue/message-status",
//                update
//        );
//    }
//
//    @Override
//    public void publishReadUpdate(
//            Long messageId
//    ) {
//
//        MessageReceipt receipt =
//                messageReceiptService
//                        .findReceiptForStatusNotification(
//                                messageId
//                        );
//
//        if (receipt == null) {
//            return;
//        }
//
//        Message message =
//                receipt.getMessage();
//
//        MessageStatusUpdate update =
//                new MessageStatusUpdate(
//                        message.getId(),
//                        message.getConversation().getId(),
//                        receipt.getUserId(),
//                        receipt.getDeliveredAt(),
//                        receipt.getReadAt()
//                );
//
//        sendToUser(
//                message.getSenderId(),
//                "/queue/message-status",
//                update
//        );
//    }
//
//    @Override
//    public void publishConversationReadUpdate(
//            Long conversationId,
//            Long readerId
//    ) {
//
//        /*
//         * This implementation can be improved later by returning
//         * the updated result directly from the receipt service.
//         *
//         * For now, the REST/STOMP controller can publish the
//         * ConversationReadUpdate returned by markConversationRead.
//         */
//    }

    @Override
    public void publishPresenceUpdate(
            Long userId,
            boolean online
    ) {

        var conversations =
                conversationRepository
                        .findUserConversations(userId);

        PresenceUpdate update =
                new PresenceUpdate(
                        userId,
                        online,
                        LocalDateTime.now()
                );

        for (Conversation conversation : conversations) {

            Long otherUserId;

            if (conversation
                    .getParticipantOneId()
                    .equals(userId)) {

                otherUserId =
                        conversation
                                .getParticipantTwoId();

            } else {

                otherUserId =
                        conversation
                                .getParticipantOneId();
            }

            /*
             * Only send presence changes to currently connected
             * counterparts.
             */
            if (presenceService
                    .isOnline(otherUserId)) {

                sendToUser(
                        otherUserId,
                        "/queue/presence",
                        update
                );
            }
        }
    }

    @Override
    public void notifySenderOfStatus(
            MessageReceipt receipt
    ) {

        Message message =
                receipt.getMessage();

        MessageStatusUpdate update =
                new MessageStatusUpdate(
                        message.getId(),
                        message.getConversation().getId(),
                        receipt.getUserId(),
                        receipt.getDeliveredAt(),
                        receipt.getReadAt()
                );

        sendToUser(
                message.getSenderId(),
                "/queue/message-status",
                update
        );
    }

    @Override
    public void notifySenderConversationRead(
            ConversationReadResult result,
            Long senderId
    ) {

        ConversationReadUpdate update =
                new ConversationReadUpdate(
                        result.conversationId(),
                        result.readerId(),
                        result.readAt(),
                        result.messagesRead(),
                        result.messageIds()
                );

        sendToUser(
                senderId,
                "/queue/conversation-read",
                update
        );
    }

    private void sendToUser(
            Long userId,
            String destination,
            Object payload
    ) {

        if (!presenceService.isOnline(userId)) {
            return;
        }

        try {

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    destination,
                    payload
            );

        } catch (MessageDeliveryException ex) {

            /*
             * Persistence already succeeded.
             * Real-time delivery failure must not make
             * a successful database write look like a failed
             * business operation.
             */
            log.warn(
                    "Real-time delivery failed. user={}, destination={}",
                    userId,
                    destination,
                    ex
            );
        }
    }

    private void sendToUserSession(
            Long userId,
            String sessionId,
            String destination,
            Object payload
    ) {

        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.create(
                        SimpMessageType.MESSAGE
                );

        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                destination,
                payload,
                accessor.getMessageHeaders()
        );
    }
}

