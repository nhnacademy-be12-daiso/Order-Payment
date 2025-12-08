package com.nhnacademy.order_payments.dto.order;

import java.util.List;

public record BookInfoResponse(
        List<BookInfo> bookInfos
) {
}
