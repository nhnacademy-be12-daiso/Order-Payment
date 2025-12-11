package com.nhnacademy.order_payments.dto.order;

import java.util.List;

/**
 * [결제하기] 버튼을 눌렀을때 넘어오는 '확정된' 주문 정보 DTO
 * 이거 토대로 주문 로직 수행하면 됨
 */
public record OrderSummaryDto (
        List<BookSummaryDto> bookList,
        OrdererSummaryDto ordererSummaryDto,
        DeliverySummaryDto deliverySummaryDto,
        Long usedPoint,
        Long savedPoint,
        Long totalPrice
) {}
