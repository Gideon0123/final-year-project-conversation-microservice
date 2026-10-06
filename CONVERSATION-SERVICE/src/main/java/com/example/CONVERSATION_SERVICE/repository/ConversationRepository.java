package com.example.CONVERSATION_SERVICE.repository;

import com.example.CONVERSATION_SERVICE.entity.Conversation;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByParticipantOneIdAndParticipantTwoId(
            Long participantOneId,
            Long participantTwoId
    );

    @Query("""
       SELECT c
       FROM Conversation c
       WHERE c.participantOneId = :userId
          OR c.participantTwoId = :userId
       ORDER BY c.updatedAt DESC
       """)
    List<Conversation> findUserConversations(
            @Param("userId") Long userId
    );
}