//package com.nhnacademy.order_payments.model;
//
//public enum PaymentMethod {
//    CARD,
//    EASY_PAY,
//    VIRTUAL_ACCOUNT
//}


package com.nhnacademy.order_payments.model;

import java.util.Objects;

public enum PaymentMethod {
    CARD("카드"),
    EASY_PAY("간편결제"),
    VIRTUAL_ACCOUNT("가상계좌");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Toss에서 넘어오는 method 문자열을 enum으로 변환
    public static PaymentMethod fromTossMethod(String method) {
        if (method == null) {
            throw new IllegalArgumentException("결제 수단이 null 입니다.");
        }

        // 1) 혹시 "CARD", "EASY_PAY" 같이 들어오면 그대로 매칭
        for (PaymentMethod value : values()) {
            if (value.name().equalsIgnoreCase(method)) {
                return value;
            }
        }

        // 2) "카드", "간편결제", "가상계좌" 같이 들어오면 displayName으로 매칭
        for (PaymentMethod value : values()) {
            if (Objects.equals(value.displayName, method)) {
                return value;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 결제 수단: " + method);
    }
}
