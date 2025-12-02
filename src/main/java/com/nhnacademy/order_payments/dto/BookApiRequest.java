package com.nhnacademy.order_payments.dto;

import java.util.List;

public record BookApiRequest(
        List<Long> bookIdList) {
}
