package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "PointHistories")
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentHistoryId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    //회원이 아직 없음
    private Long userCreated;
//    @ManyToOne
//    @JoinColumn(name = "user_created_id")
//    private Member userCreated;

    private Integer usedPoint;

    private LocalDateTime usedAt;

    @Builder
    public PointHistory(Order order, Long userCreated, Integer usedPoint, LocalDateTime usedAt) {
        this.order = order;
        this.userCreated = userCreated;
        this.usedPoint = usedPoint;
        this.usedAt = usedAt;
    }
}
