package com.nhnacademy.order_payments.saga.event;

import org.springframework.context.ApplicationEvent;

// OutboxCommittedEvent.java
public class OrderOutboxCommittedEvent extends ApplicationEvent {
    private final Long outboxId;

    public OrderOutboxCommittedEvent(Object source, Long outboxId) {
        super(source);
        this.outboxId = outboxId;
    }
    public Long getOutboxId() {
        return outboxId;
    }
}
