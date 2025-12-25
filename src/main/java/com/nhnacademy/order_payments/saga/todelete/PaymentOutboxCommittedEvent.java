package com.nhnacademy.order_payments.saga.todelete;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentOutboxCommittedEvent extends ApplicationEvent {
    private final Long outboxId;

    public PaymentOutboxCommittedEvent(Object source, Long outboxId) {
        super(source);
        this.outboxId = outboxId;
    }
}
