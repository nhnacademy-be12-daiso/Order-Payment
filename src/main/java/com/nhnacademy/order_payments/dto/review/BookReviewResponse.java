package com.nhnacademy.order_payments.dto.review;

public record BookReviewResponse(
        BookResponse book,
        long orderDetailId,
        Long reviewId
) {
}
