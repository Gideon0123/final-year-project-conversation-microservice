package com.example.CONVERSATION_SERVICE.mapper;

import com.example.CONVERSATION_SERVICE.dto.MessageResponse;
import com.example.CONVERSATION_SERVICE.entity.Conversation;
import com.example.CONVERSATION_SERVICE.entity.Message;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-06T02:24:13+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class MessageMapperImpl implements MessageMapper {

    @Override
    public MessageResponse toResponse(Message message) {
        if ( message == null ) {
            return null;
        }

        Long conversationId = null;
        Long id = null;
        Long senderId = null;
        String content = null;
        LocalDateTime createdAt = null;

        conversationId = messageConversationId( message );
        id = message.getId();
        senderId = message.getSenderId();
        content = message.getContent();
        createdAt = message.getCreatedAt();

        Long recipientId = null;
        LocalDateTime deliveredAt = null;
        LocalDateTime readAt = null;

        MessageResponse messageResponse = new MessageResponse( id, conversationId, senderId, content, createdAt, recipientId, deliveredAt, readAt );

        return messageResponse;
    }

    private Long messageConversationId(Message message) {
        Conversation conversation = message.getConversation();
        if ( conversation == null ) {
            return null;
        }
        return conversation.getId();
    }
}
