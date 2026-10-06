package com.example.CONVERSATION_SERVICE.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "message_receipts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_message_receipt_message_user",
                        columnNames = {
                                "message_id",
                                "user_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_message_receipt_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_message_receipt_message",
                        columnList = "message_id"
                ),
                @Index(
                        name = "idx_message_receipt_user_delivered_read",
                        columnList = "user_id, delivered_at, read_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "message_id",
            nullable = false
    )
    private Message message;

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public void markDelivered() {
        if (deliveredAt == null) {
            deliveredAt = LocalDateTime.now();
        }
    }

    public void markRead() {
        if (deliveredAt == null) {
            deliveredAt = LocalDateTime.now();
        }

        if (readAt == null) {
            readAt = LocalDateTime.now();
        }
    }
}
