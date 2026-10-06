package com.example.CONVERSATION_SERVICE.repository;

import com.example.CONVERSATION_SERVICE.entity.MessageReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}