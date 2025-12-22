/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2025. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.order_payments.service.order;

import com.nhnacademy.order_payments.client.BookApiClient;
import com.nhnacademy.order_payments.dto.response.order.OrderDetailResponse;
import com.nhnacademy.order_payments.dto.response.order.OrderListResponse;
import com.nhnacademy.order_payments.dto.response.order.OrderResponse;
import com.nhnacademy.order_payments.dto.review.BookOrderDetailRequest;
import com.nhnacademy.order_payments.dto.review.BookReviewRequest;
import com.nhnacademy.order_payments.dto.review.BookReviewResponse;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.service.packaging.PackagingService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderResultService {

    private final OrderRepository orderRepository;

    private final PackagingService packagingService;

    private final BookApiClient bookApiClient;

    @Transactional(readOnly = true)
    public OrderListResponse getOrderList(Long userId, String guestId) {
        // 주문 목록 조회 (회원/비회원 분기)
        List<Order> orderList;

        if (guestId == null) {
            orderList = orderRepository.findOrderByUserId(userId);  // 최신순 정렬 고려
        } else {
            orderList = orderRepository.findOrderByGuestId(guestId);
        }

        if (orderList == null || orderList.isEmpty()) {
            return new OrderListResponse(List.of());
        }

        List<OrderResponse> responseList = orderList.stream()
                .map(this::createOrderResponse)
                .toList();

        return new OrderListResponse(responseList);
    }

    private OrderResponse createOrderResponse(Order order) {
        List<OrderDetail> orderDetailList = order.getOrderDetailList();

        // 도서 정보 및 리뷰 정보 조회
        // 한 번의 주문에 포함된 모든 책 ID를 수집하여 일괄 조회
        BookReviewRequest reviewRequest = new BookReviewRequest(order.getUserId(),
                orderDetailList.stream()
                        .map(od -> new BookOrderDetailRequest(od.getBookId(), od.getId()))
                        .toList());

        Map<Long, BookReviewResponse> bookInfoMap = bookApiClient.getBookReviewList(reviewRequest).stream()
                .collect(Collectors.toMap(BookReviewResponse::orderDetailId, b -> b));

        // 주문 상세 DTO 리스트 생성
        List<OrderDetailResponse> detailResponses = orderDetailList.stream()
                .map(od -> {
                    BookReviewResponse bookInfo = bookInfoMap.get(od.getId());

                    String title = "정보 없음";
                    String imgUrl = null;
                    Long reviewId = null;

                    if (bookInfo != null && bookInfo.book() != null) {
                        title = bookInfo.book().title();

                        if (!bookInfo.book().imageList().isEmpty()) {
                            imgUrl = bookInfo.book().imageList().getFirst().path();
                        }

                        reviewId = bookInfo.reviewId();
                    }

                    return new OrderDetailResponse(od.getId(),
                            od.getBookId(), title, imgUrl, od.getPrice(), od.getQuantity(),
                            packagingService.getPackagingName(od.getPackagingId()), od.getOrderDetailStatus(),
                            reviewId);
                }).toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderStatus(),
                order.getOrderDate(),
                order.getOrdererName(),
                order.getTotalPrice(),
                order.getPhoneNumber(),
                order.getEmail(),
                detailResponses);
    }

}