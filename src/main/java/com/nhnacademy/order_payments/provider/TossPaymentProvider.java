package com.nhnacademy.order_payments.provider;

import com.nhnacademy.order_payments.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

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
                .uri("v1/payments/confirm")   // base-url: https://api.tosspayments.com
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

        if (res == null) {
            log.error("[TOSS ERROR] Response body is NULL. orderId={}", cmd.orderId());
            throw new BusinessException("TOSS_NO_RESPONSE", "토스 API 응답이 비어있습니다. (URL이나 키 설정을 확인하세요)");
        }

        // 간편결제일때 KAKAOPAY/NAVERPAY/TOSSPAY 값이 들어옴
        String methodDetail = null;
        if (res.getEasyPay() != null && res.getEasyPay().getProvider() != null) {
            methodDetail = res.getEasyPay().getProvider();
        }

        // 응답
        log.info("[TOSS CONFIRM RESPONSE] method={}, approvedAt={}, easyPayProvider={}",
                res.getMethod(), res.getApprovedAt(), methodDetail);

        // 파사드에 넘겨줄 DTO 변환
        return new ApproveResult(
                "TOSS",
                res.getMethod(),
                res.getApprovedAt(),
                methodDetail
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
                .uri("v1/payments/{paymentKey}/cancel", cmd.paymentKey())
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
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TossConfirmResponse {
        private String method;
        private String approvedAt;
        private EasyPay easyPay;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EasyPay {
        private String provider; // "TOSSPAY" / "KAKAOPAY" / "NAVERPAY"
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TossCancelResponse {
        private String method;
        private String canceledAt;
    }
}
