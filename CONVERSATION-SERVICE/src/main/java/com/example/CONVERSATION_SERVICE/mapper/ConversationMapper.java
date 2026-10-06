package com.example.CONVERSATION_SERVICE.mapper;
import com.example.CONVERSATION_SERVICE.dto.ConversationResponse;
import com.example.CONVERSATION_SERVICE.entity.Conversation;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    ConversationResponse toResponse(
            Conversation conversation
    );
}