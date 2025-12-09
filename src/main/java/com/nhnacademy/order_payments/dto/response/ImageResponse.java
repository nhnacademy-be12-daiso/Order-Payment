package com.nhnacademy.order_payments.dto.response;

public record ImageResponse(
        long no,
        String path,
        ImageType imageType) {
}