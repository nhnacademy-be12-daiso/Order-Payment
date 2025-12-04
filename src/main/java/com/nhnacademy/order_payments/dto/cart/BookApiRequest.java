package com.nhnacademy.order_payments.dto.cart;

import java.util.List;

public record BookApiRequest(
        List<Long> bookIdList) {
}
