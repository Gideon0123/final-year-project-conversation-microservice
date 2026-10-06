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
    @Mapping(
            target = "recipientId",
            ignore = true
    )
    @Mapping(
            target = "deliveredAt",
            ignore = true
    )
    @Mapping(
            target = "readAt",
            ignore = true
    )
    MessageResponse toResponse(Message message);
}