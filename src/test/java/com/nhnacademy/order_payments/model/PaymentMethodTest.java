package com.nhnacademy.order_payments.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentMethodTest {

    // enum 이름과 정확히 일치하는지 확인
    @Test
    void fromTossMethod_matchesEnumName_ignoreCase() {
        assertEquals(PaymentMethod.CARD, PaymentMethod.fromTossMethod("card"));
        assertEquals(PaymentMethod.EASY_PAY, PaymentMethod.fromTossMethod("EASY_PAY"));
    }

    // 한글 표시로도 매핑되는지 확인
    @Test
    void fromTossMethod_matchesDisplayName_korean() {
        assertEquals(PaymentMethod.CARD, PaymentMethod.fromTossMethod("카드"));
        assertEquals(PaymentMethod.EASY_PAY, PaymentMethod.fromTossMethod("간편결제"));
    }

    // null 입력 시 예외 발생 확인
    @Test
    void fromTossMethod_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> PaymentMethod.fromTossMethod(null));
    }

    @Test
    void fromTossMethod_unknown_throws() {
        assertThrows(IllegalArgumentException.class, () -> PaymentMethod.fromTossMethod("BITCOIN"));
    }
}
