package com.nhnacademy.order_payments.provider;

public interface PaymentProvider {
    record ApproveCommand(String orderId, String paymentKey, int amount) {} //승인요청
    record ApproveResult(String provider, String method, String approvedAtIso) {} //승인정보

    record CancelCommand(String orderId, String paymentKey, Integer cancelAmount, String reason) {} //취소요청
    record CancelResult(String provider, String method, String canceledAtIso) {} //취소정보

    ApproveResult approve(ApproveCommand cmd);

    CancelResult cancel(CancelCommand cmd);
}
