package com.example.CONVERSATION_SERVICE.repository;

import com.example.CONVERSATION_SERVICE.entity.MessageReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageReceiptRepository
        extends JpaRepository<MessageReceipt, Long> {

    Optional<MessageReceipt> findByMessageIdAndUserId(
            Long messageId,
            Long userId
    );

    boolean existsByMessageIdAndUserId(
            Long messageId,
            Long userId
    );

    /*
     * Used when loading message history.
     *
     * A page of messages can contain messages belonging to
     * both participants, so we fetch receipts for both users
     * in one query instead of generating N+1 queries.
     */
    @Query("""
            SELECT r
            FROM MessageReceipt r
            JOIN FETCH r.message m
            WHERE m.id IN :messageIds
              AND r.userId IN :userIds
            """)
    List<MessageReceipt> findByMessageIdsAndUserIds(
            @Param("messageIds")
            Collection<Long> messageIds,

            @Param("userIds")
            Collection<Long> userIds
    );

    /*
     * Undelivered messages for reconnect synchronization.
     *
     * The message itself is fetched eagerly through JOIN FETCH
     * so synchronization does not generate an N+1 query.
     */
    @Query("""
            SELECT r
            FROM MessageReceipt r
            JOIN FETCH r.message m
            JOIN FETCH m.conversation c
            WHERE r.userId = :userId
              AND r.deliveredAt IS NULL
            ORDER BY m.createdAt ASC, m.id ASC
            """)
    List<MessageReceipt> findUndeliveredMessages(
            @Param("userId")
            Long userId
    );

    /*
     * All currently unread messages in one conversation.
     */
    @Query("""
            SELECT r
            FROM MessageReceipt r
            JOIN FETCH r.message m
            WHERE m.conversation.id = :conversationId
              AND r.userId = :userId
              AND r.readAt IS NULL
            ORDER BY m.createdAt ASC, m.id ASC
            """)
    List<MessageReceipt> findUnreadMessages(
            @Param("conversationId")
            Long conversationId,

            @Param("userId")
            Long userId
    );

    @Query("""
            SELECT COUNT(r)
            FROM MessageReceipt r
            WHERE r.userId = :userId
              AND r.readAt IS NULL
            """)
    long countUnreadByUser(
            @Param("userId")
            Long userId
    );

    @Query("""
            SELECT COUNT(r)
            FROM MessageReceipt r
            WHERE r.userId = :userId
              AND r.message.conversation.id = :conversationId
              AND r.readAt IS NULL
            """)
    long countUnreadByConversation(
            @Param("conversationId")
            Long conversationId,

            @Param("userId")
            Long userId
    );

    /*
     * Efficiently obtains unread counts for the user's entire
     * conversation list in one query.
     */
    @Query("""
            SELECT
                r.message.conversation.id AS conversationId,
                COUNT(r) AS unreadCount
            FROM MessageReceipt r
            WHERE r.userId = :userId
              AND r.readAt IS NULL
            GROUP BY r.message.conversation.id
            """)
    List<ConversationUnreadCountProjection> findUnreadCountsByUser(
            @Param("userId")
            Long userId
    );
}