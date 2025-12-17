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
@Table(name = "payment_deduplication_log")
@EntityListeners(AuditingEntityListener.class) // 생성일자 자동 기록을 위해 사용
public class PaymentDeduplicationLog {

    /**
     * 수신한 이벤트 메시지의 고유 ID를 Primary Key로 사용합니다.
     * 이 필드에 중복 값이 삽입되면 DB 제약 조건에 의해 예외가 발생하며,
     * 이는 Spring의 트랜잭션을 롤백시키고 중복 처리를 확실하게 막습니다.
     */
    @Id
    @Column(name = "message_id", length = 128) // RabbitMQ 메시지 ID 또는 이벤트 ID를 가정
    private Long messageId;

    /**
     * 메시지를 처음 수신하고 처리 로그를 기록한 시간입니다.
     */
    @CreatedDate
    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    /**
     * 생성자: 메시지 ID를 받아 로그를 생성합니다.
     * @param messageId 수신한 이벤트의 고유 ID
     */
    public PaymentDeduplicationLog(Long messageId) {
        this.messageId = messageId;
        this.receivedAt = LocalDateTime.now(); // @CreatedDate가 아닌 경우 수동으로 설정
    }


}