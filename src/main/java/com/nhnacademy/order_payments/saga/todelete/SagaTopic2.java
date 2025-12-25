package com.nhnacademy.order_payments.saga.todelete;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SagaTopic2 {

    // 성공 트랜잭션

    // order -> book
    BOOK_CHECKOUT("team3.saga.order.exchange", "team3.saga.book.checkout", "saga.command.book.checkout"),
    // order -> user
    USER_POINTS("team3.saga.order.exchange", "team3.saga.user.point-deduct", "saga.command.user.point-deduct"),
    // order -> coupon
    COUPON_USE("team3.saga.order.exchange", "team3.saga.coupon.use", "saga.command.coupon.use"),
    // 각 서비스 -> order
    ORDER_REPLY("team3.saga.reply.exchange", "team3.saga.order.reply", "saga.reply.order"),


//    BOOK_ROLLBACK("team3.saga.order.exchange", "team3.saga.compensate.compensate"),
//    USER_ROLLBACK(),
//    COUPON_ROLLBACK(),












    // 보상 트랜잭션 시
    // book -> order
    BOOK_COMPENSATION("team3.saga.book.exchange", "team3.saga.order.compensate.queue", "book.compensate"),
    // user -> book
    USER_COMPENSATION("team3.saga.user.exchange", "team3.saga.book.compensate.queue", "point.compensate"),
    // coupon -> user
    COUPON_COMPENSATION("team3.saga.coupon.exchange", "team3.saga.user.compensate.queue", "coupon.compensate"),
    // payment -> coupon
    PAYMENT_COMPENSATION("team3.saga.payment.exchange", "team3.saga.coupon.compensate.queue", "payment.compensate"),

    // OrderAPI로 우선 알림
    USER_NOTIFICATION("team3.saga.user.exchange", "team3.saga.order.notice.queue", "point.notice"),
    // coupon -> user
    COUPON_NOTIFICATION("team3.saga.coupon.exchange", "team3.saga.order.notice.queue", "coupon.notice"),
    // payment -> coupon
    PAYMENT_NOTIFICATION("team3.saga.payment.exchange", "team3.saga.order.notice.queue", "payment.notice");


    private final String exchange;
    private final String queue;
    private final String routingKey;

    private static boolean isDevMode = false;

    public static void setMode(boolean isDev) {
        isDevMode = isDev;
    }

    public String getQueue() {
        return isDevMode ?  queue + ".dev" : queue;
    }

    public String getRoutingKey() {
        return isDevMode ? routingKey + ".dev" : routingKey;
    }
}

