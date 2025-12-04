package com.nhnacademy.order_payments.dto.review;

import java.util.List;

public record BookReviewRequest(
        Long userId,
        List<BookOrderDetailRequest> bookOrderDetailRequests) {
}
