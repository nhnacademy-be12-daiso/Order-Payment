package com.nhnacademy.order_payments.dto.review;

import com.nhnacademy.order_payments.dto.response.ImageResponse;

import java.util.List;

public record BookResponse(
        long bookId,
        String title,
        List<ImageResponse> imageList
) {}
