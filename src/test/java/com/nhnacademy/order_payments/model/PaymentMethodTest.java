package com.nhnacademy.order_payments.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentMethodTest {

    @Test
    void fromTossMethod_matchesEnumName_ignoreCase() {
        assertEquals(PaymentMethod.CARD, PaymentMethod.fromTossMethod("card"));
        assertEquals(PaymentMethod.EASY_PAY, PaymentMethod.fromTossMethod("EASY_PAY"));
    }

    @Test
    void fromTossMethod_matchesDisplayName_korean() {
        assertEquals(PaymentMethod.CARD, PaymentMethod.fromTossMethod("카드"));
        assertEquals(PaymentMethod.EASY_PAY, PaymentMethod.fromTossMethod("간편결제"));
    }

    @Test
    void fromTossMethod_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> PaymentMethod.fromTossMethod(null));
    }

    @Test
    void fromTossMethod_unknown_throws() {
        assertThrows(IllegalArgumentException.class, () -> PaymentMethod.fromTossMethod("BITCOIN"));
    }
}
