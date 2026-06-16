package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_deduplication_log",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_dedup_message_id", columnNames = "message_id"))
@EntityListeners(AuditingEntityListener.class)
public class OrderDeduplicationLog {

    @Id
    @Column(name = "message_id", length = 128)
    private String messageId;

    @CreatedDate
    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    public OrderDeduplicationLog(String messageId) {
        this.messageId = messageId;
        this.receivedAt = LocalDateTime.now(); // @CreatedDate가 아닌 경우 수동으로 설정
    }
}