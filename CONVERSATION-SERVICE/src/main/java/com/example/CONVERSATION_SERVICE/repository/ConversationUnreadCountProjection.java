package com.example.CONVERSATION_SERVICE.repository;

public interface ConversationUnreadCountProjection {

    Long getConversationId();

    long getUnreadCount();
}