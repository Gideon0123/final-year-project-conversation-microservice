package com.example.CONVERSATION_SERVICE.service;

import com.example.CONVERSATION_SERVICE.dto.MessageResponse;
import com.example.CONVERSATION_SERVICE.dto.SendMessageResult;
import com.example.CONVERSATION_SERVICE.payload.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    SendMessageResult sendMessage(
            Long conversationId,
            Long senderId,
            String content
    );

    PagedResponse<MessageResponse> getMessages(
            Long conversationId,
            Long currentUserId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );
}