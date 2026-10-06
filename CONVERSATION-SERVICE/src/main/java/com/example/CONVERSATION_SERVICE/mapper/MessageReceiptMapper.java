package com.example.CONVERSATION_SERVICE.mapper;

import com.example.CONVERSATION_SERVICE.dto.MessageReceiptResponse;
import com.example.CONVERSATION_SERVICE.entity.MessageReceipt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageReceiptMapper {

    @Mapping(
            target = "messageId",
            source = "message.id"
    )
    MessageReceiptResponse toResponse(
            MessageReceipt receipt
    );
}