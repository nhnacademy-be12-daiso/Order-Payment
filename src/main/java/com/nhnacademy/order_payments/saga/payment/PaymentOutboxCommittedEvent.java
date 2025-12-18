package com.nhnacademy.order_payments.saga.payment;

import org.springframework.context.ApplicationEvent;

public class PaymentOutboxCommittedEvent extends ApplicationEvent {
    private final Long outboxId;

    public PaymentOutboxCommittedEvent(Object source, Long outboxId) {
        super(source);
        this.outboxId = outboxId;
    }
    public Long getOutboxId() {
        return outboxId;
    }
}
