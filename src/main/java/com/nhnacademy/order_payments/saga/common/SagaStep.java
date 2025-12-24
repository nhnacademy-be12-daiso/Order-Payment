package com.nhnacademy.order_payments.saga.common;

import com.nhnacademy.order_payments.entity.SagaInstance;
import com.nhnacademy.order_payments.saga.SagaOrchestrator;
import com.nhnacademy.order_payments.saga.event.OrderConfirmedEvent;
import lombok.Getter;


/**
 * 각 서비스를 Enum으로 관리해서 편하게 운용하려고 함
 * 기본적으로 가진 값 : (순서, Routing Key, 보상 Routing Key)
 *
 * 사용되는 메서드
 * updateStatus() : 상태 변경
 * execute() : 메세지 전송
 * getStatus() : 상태 가져오기
 */

public enum SagaStep {

    BOOK_CHECKOUT(1, SagaTopic.BOOK_RK, SagaTopic.BOOK_COMPENSATION_RK) {
        @Override
        public void updateStatus(SagaInstance instance, ServiceStatus status) {
            instance.setBookStatus(status); // 자기가 알아서 Book 필드를 수정
        }
        @Override
        public void execute(SagaOrchestrator orchestrator, OrderConfirmedEvent event) {
            orchestrator.send(SagaTopic.BOOK_RK,  event);
        }
        @Override
        public ServiceStatus getStatus(SagaInstance instance) {
            return instance.getBookStatus();
        }
    },

    USER_POINTS(2, SagaTopic.USER_RK, SagaTopic.USER_COMPENSATION_RK) {
        @Override
        public void updateStatus(SagaInstance instance, ServiceStatus status) {
            instance.setBookStatus(status);
        }
        @Override
        public void execute(SagaOrchestrator orchestrator, OrderConfirmedEvent event) {
            orchestrator.send(SagaTopic.USER_RK, event);
        }
        @Override
        public ServiceStatus getStatus(SagaInstance instance) {
            return instance.getUserStatus();
        }
    },

//    COUPON_USE(3) {
    COUPON_USE(3, SagaTopic.COUPON_RK, SagaTopic.COUPON_COMPENSATION_RK) {
        @Override
        public void updateStatus(SagaInstance instance, ServiceStatus status) {
            instance.setBookStatus(status);
        }
        @Override
        public void execute(SagaOrchestrator orchestrator, OrderConfirmedEvent event) {
            orchestrator.send(SagaTopic.COUPON_RK,  event);
        }
        @Override
        public ServiceStatus getStatus(SagaInstance instance) {
            return instance.getCouponStatus();
        }
    },

    FINISHED(4, null, null) {
        @Override
        public void updateStatus(SagaInstance instance, ServiceStatus status) {
            instance.setBookStatus(status); // 자기가 알아서 Book 필드를 수정
        }
        @Override public void execute(SagaOrchestrator orchestrator, OrderConfirmedEvent ev) {}
        @Override
        public ServiceStatus getStatus(SagaInstance instance) {
            return null; // 얘는 굳이 반환할 필요가 없잖..? 아?
        }
    };


    public abstract void updateStatus(SagaInstance instance, ServiceStatus status);
    public abstract void execute(SagaOrchestrator orchestrator, OrderConfirmedEvent event);
    public abstract ServiceStatus getStatus(SagaInstance instance);

    @Getter
    private final int order; // 순서를 정의하는 듯?
    @Getter
    private final String key; // 성공 시 라우팅 키
    @Getter
    private final String rollbackKey; // 실패 시 라우팅 키

    SagaStep(int order, String key, String rollbackKey) {
        this.order = order;
        this.key = key;
        this.rollbackKey = rollbackKey;
    }


    public SagaStep next() {
        SagaStep[] steps = SagaStep.values();
        int nextOrdinal = this.ordinal() + 1;

        if (nextOrdinal < steps.length) {
            return steps[nextOrdinal];
        }
        return FINISHED;
    }
}
