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
import com.nhnacademy.order_payments.entity.GuestOrderers;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.model.OrderDetailStatus;
import com.nhnacademy.order_payments.repository.GuestOrdererRepository;
import com.nhnacademy.order_payments.repository.OrderDetailRepository;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.saga.SagaOrchestrator;
import com.nhnacademy.order_payments.saga.event.OrderRefundEvent;
import com.nhnacademy.order_payments.service.packaging.PackagingService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderResultService {

    private final OrderRepository orderRepository;
    private final GuestOrdererRepository guestOrdererRepository;

    private final PackagingService packagingService;

    private final BookApiClient bookApiClient;

    private final PasswordEncoder passwordEncoder;
    private final OrderDetailRepository orderDetailRepository;

    private final SagaOrchestrator sagaOrchestrator;

    @Transactional(readOnly = true)
    public OrderListResponse getOrderList(Long userId) {
        // 주문 목록 조회 (회원)
        List<Order> orderList;

        orderList = orderRepository.findOrderByUserId(userId);  // 최신순 정렬 고려

        if (orderList == null || orderList.isEmpty()) {
            return new OrderListResponse(List.of());
        }

        List<OrderResponse> responseList = orderList.stream()
                .map(this::createOrderResponse)
                .toList();

        return new OrderListResponse(responseList);
    }

    @Transactional(readOnly = true)
    public OrderResponse getGuestOrder(Long orderNumber, String password) {
        // 주문 조회 (비회원)
        // 비회원은 주문 번호(order_number)와 주문서 작성시 입력했던 비밀번호(orderer_password)로 주문 조회
        GuestOrderers guestInfo = guestOrdererRepository.findByOrder_OrderNumber(orderNumber)
                .orElseThrow(() -> {
                    log.warn("[OrderResultService] 비회원 - 찾을 수 없는 주문: {}", orderNumber);
                    return new NotFoundOrderException("[OrderResultService] 비회원 - 찾을 수 없는 주문");
                });

        if (!passwordEncoder.matches(password, guestInfo.getPassword())) {
            throw new RuntimeException("[OrderResultService] 비회원 - 일치하지 않는 비밀번호");
        }

        return createOrderResponse(guestInfo.getOrder());
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
                .collect(Collectors.toMap(br -> br.book().bookId(), b -> b));

        // 주문 상세 DTO 리스트 생성
        List<OrderDetailResponse> detailResponses = orderDetailList.stream()
                .map(od -> {
                    BookReviewResponse bookInfo = bookInfoMap.get(od.getBookId());

                    String title = "정보 없음";
                    String imgUrl = null;
                    Long reviewId = null;

                    if (bookInfo != null && bookInfo.book() != null) {
                        title = bookInfo.book().title();

                        if (bookInfo.book().imageList() != null && !bookInfo.book().imageList().isEmpty()) {
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

    @Transactional
    public void refundOrder(Long orderDetailId) {
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId).orElseThrow();

        Long refundAmount = orderDetail.getPrice() * orderDetail.getQuantity() - 3000;
        // ---> 배송비 제외하고 반품 가격 책정 (할인가 문제 해결해야함)

        OrderRefundEvent event = new OrderRefundEvent(orderDetail, refundAmount);

        sagaOrchestrator.start(event);

        orderDetail.setOrderDetailStatus(OrderDetailStatus.RETURNED);

    }

}