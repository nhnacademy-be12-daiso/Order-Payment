package com.nhnacademy.order_payments.provider;

import com.nhnacademy.order_payments.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Profile("dev-toss")
@Component
@RequiredArgsConstructor
@Slf4j
public class TossPaymentProvider implements PaymentProvider {

    private final WebClient tossWebClient;

    @Override
    public ApproveResult approve(ApproveCommand cmd) {
        Map<String, Object> body = Map.of(
                "paymentKey", cmd.paymentKey(),
                "orderId", cmd.orderId(),
                "amount", cmd.amount()
        );

        // 결제 요청 로그 남기는 부분
        log.info("[TOSS CONFIRM REQUEST] orderId={}, paymentKey={}, amount={}",
                cmd.orderId(), cmd.paymentKey(), cmd.amount());

        // 토스 confirm API 호출
        TossConfirmResponse res = tossWebClient.post()
                .uri("/confirm")   // base-url: https://api.tosspayments.com/v1/payments
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .doOnNext(errorBody -> log.error(
                                        "[TOSS CONFIRM ERROR] status={}, body={}",
                                        clientResponse.statusCode(), errorBody))
                                .flatMap(errorBody -> Mono.error(
                                        new BusinessException("TOSS_CONFIRM_FAILED",
                                                "토스 결제 승인 API 호출에 실패했습니다.")
                                ))
                )
                .bodyToMono(TossConfirmResponse.class)
                .block();   // 동기 호출

        // 응답
        log.info("[TOSS CONFIRM RESPONSE] method={}, approvedAt={}",
                res.getMethod(), res.getApprovedAt());

        // 파사드에 넘겨줄 DTO 변환
        return new ApproveResult(
                "TOSS",
                res.getMethod(),
                res.getApprovedAt()
        );
    }

    @Override
    public CancelResult cancel(CancelCommand cmd) {
        Map<String, Object> body = Map.of(
                "cancelReason", cmd.reason(),
                "cancelAmount", cmd.cancelAmount()
        );

        log.info("[TOSS CANCEL REQUEST] orderId={}, paymentKey={}, cancelAmount={}, reason={}",
                cmd.orderId(), cmd.paymentKey(), cmd.cancelAmount(), cmd.reason());

        TossCancelResponse res = tossWebClient.post()
                .uri("/{paymentKey}/cancel", cmd.paymentKey())
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .doOnNext(errorBody -> log.error(
                                        "[TOSS CANCEL ERROR] status={}, body={}",
                                        clientResponse.statusCode(), errorBody))
                                .flatMap(errorBody -> Mono.error(
                                        new BusinessException("TOSS_CANCEL_FAILED",
                                                "토스 결제 취소 API 호출에 실패했습니다.")
                                ))
                )
                .bodyToMono(TossCancelResponse.class)
                .block();

        log.info("[TOSS CANCEL RESPONSE] method={}, canceledAt={}",
                res.getMethod(), res.getCanceledAt());

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

        public String getMethod() { return method; }
        public String getApprovedAt() { return approvedAt; }
    }

    public static class TossCancelResponse {
        private String method;
        private String canceledAt;

        public String getMethod() { return method; }
        public String getCanceledAt() { return canceledAt; }
    }
}
