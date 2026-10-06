package com.example.CONVERSATION_SERVICE.mapper;

import com.example.CONVERSATION_SERVICE.dto.ConversationResponse;
import com.example.CONVERSATION_SERVICE.entity.Conversation;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-05T12:37:09+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class ConversationMapperImpl implements ConversationMapper {

    @Override
    public ConversationResponse toResponse(Conversation conversation) {
        if ( conversation == null ) {
            return null;
        }

        Long id = null;
        Long participantOneId = null;
        Long participantTwoId = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = conversation.getId();
        participantOneId = conversation.getParticipantOneId();
        participantTwoId = conversation.getParticipantTwoId();
        createdAt = conversation.getCreatedAt();
        updatedAt = conversation.getUpdatedAt();

        ConversationResponse conversationResponse = new ConversationResponse( id, participantOneId, participantTwoId, createdAt, updatedAt );

        return conversationResponse;
    }
}
