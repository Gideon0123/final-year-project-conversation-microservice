package com.example.CONVERSATION_SERVICE.mapper;
import com.example.CONVERSATION_SERVICE.dto.ConversationResponse;
import com.example.CONVERSATION_SERVICE.entity.Conversation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(
            target = "unreadCount",
            ignore = true
    )
    ConversationResponse toResponse(
            Conversation conversation
    );
}