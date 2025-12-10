package com.nhnacademy.order_payments.dto.response.order;

import com.nhnacademy.order_payments.model.OrderDetailStatus;

public record OrderDetailResponse(
        long orderDetailId,
        long bookId,
        String bookTitle,
        String bookCoverImage,
        int price, //구매당시 금액
        int quantity,
        String packagingName, // 포장을 안했으면 null, 했으면 포장한 포장지 이름
        OrderDetailStatus orderDetailStatus,
        Long reviewId
) {
}
