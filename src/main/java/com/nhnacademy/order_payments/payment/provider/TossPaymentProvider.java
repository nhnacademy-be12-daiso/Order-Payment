package com.nhnacademy.order_payments.payment.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Profile("dev-toss")
@Component
@RequiredArgsConstructor
public class TossPaymentProvider implements PaymentProvider {

    private final WebClient tossWebClient;

    @Override
    public ApproveResult approve(ApproveCommand cmd) {
        // 토스 결제 승인 API: POST /v1/payments/confirm
        Map<String, Object> body = Map.of(
                "paymentKey", cmd.paymentKey(),
                "orderId", cmd.orderId(),
                "amount", cmd.amount()
        );

        TossConfirmResponse res = tossWebClient.post()
                .uri("/v1/payments/confirm")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(TossConfirmResponse.class)
                .block();   // 구조상 동기 호출이라 block() 사용

        return new ApproveResult(
                "TOSS",
                res.getMethod(),
                res.getApprovedAt()
        );
    }

    @Override
    public CancelResult cancel(CancelCommand cmd) {
        // 토스 결제 취소 API: POST /v1/payments/{paymentKey}/cancel
        Map<String, Object> body = Map.of(
                "cancelReason", cmd.reason(),
                "cancelAmount", cmd.cancelAmount()
        );

        TossCancelResponse res = tossWebClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", cmd.paymentKey())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(TossCancelResponse.class)
                .block();

        return new CancelResult(
                "TOSS",
                res.getMethod(),
                res.getCanceledAt()
        );
    }

    // 토스 응답용 DTO
    public static class TossConfirmResponse {
        private String method;
        private String approvedAt;

        public String getMethod() {
            return method;
        }

        public String getApprovedAt() {
            return approvedAt;
        }
    }

    public static class TossCancelResponse {
        private String method;
        private String canceledAt;

        public String getMethod() {
            return method;
        }

        public String getCanceledAt() {
            return canceledAt;
        }
    }
}
