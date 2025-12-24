package com.nhnacademy.order_payments.entity;

import com.nhnacademy.order_payments.saga.common.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "saga_instance")
@NoArgsConstructor
@Getter
public class SagaInstance {

    @Id
    @Column(name = "saga_id")
    private String sagaId;

    @Setter
    private SagaStatus sagaStatus = SagaStatus.STARTED;
    @Setter
    private ServiceStatus bookStatus = ServiceStatus.PENDING;
    @Setter
    private ServiceStatus userStatus = ServiceStatus.PENDING;
    @Setter
    private ServiceStatus couponStatus = ServiceStatus.PENDING;

    @Setter
    private SagaStep currentStep;
    // converter 써서 정수로 입력해도 알잘딱 저장해줌

    @Column(columnDefinition = "TEXT")
    private String payload; // 이벤트 원본

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public SagaInstance(String sagaId, String payload) {
        this.sagaId = sagaId;
        this.payload = payload;
    }
}
