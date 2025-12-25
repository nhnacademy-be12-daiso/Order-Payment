package com.nhnacademy.order_payments.saga.todelete;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class PaymentOutboxRelayManager {

    private final PaymentOutboxRelayProcessor paymentOutboxRelayProcessor;

    // PaymentEventListener가 커밋된 이후 실행됨
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxCommitted(PaymentOutboxCommittedEvent event) {
        paymentOutboxRelayProcessor.processRelay(event.getOutboxId());
    }


}
