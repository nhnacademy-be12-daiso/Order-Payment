package com.nhnacademy.order_payments.dto.order;

import org.springframework.core.annotation.OrderUtils;

import java.util.List;

/**
 * [결제하기] 버튼을 눌렀을때 넘어오는 '확정된' 주문 정보 DTO
 * 이거 토대로 주문 로직 수행하면 됨
 */
public class OrderSummaryDto {
    List<BookOrderUnit> bookList;


}
