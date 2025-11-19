package com.nhnacademy.order_payments.payment.provider;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

//테스트용 구현 클래스
@Profile("dev-fake")
@Component
public class FakePaymentProvider implements PaymentProvider {
    @Override
    public ApproveResult approve(ApproveCommand cmd)  {
        return new ApproveResult("FAKE", "CARD", OffsetDateTime.now().toString());
    }

    @Override
    public CancelResult cancel(CancelCommand cmd) {
        // 실제 PG 취소 대신 성공으로 가정함
        return new CancelResult("FAKE", "CARD", OffsetDateTime.now().toString());
    }
}
