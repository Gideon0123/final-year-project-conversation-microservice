package com.example.CONVERSATION_SERVICE.mapper;

import com.example.CONVERSATION_SERVICE.dto.MessageResponse;
import com.example.CONVERSATION_SERVICE.entity.Message;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(
            target = "conversationId",
            source = "conversation.id"
    )
    MessageResponse toResponse(
            Message message
    );
}