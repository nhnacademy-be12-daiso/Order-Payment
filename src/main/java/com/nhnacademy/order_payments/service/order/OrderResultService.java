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
import com.nhnacademy.order_payments.dto.cart.BookApiRequest;
import com.nhnacademy.order_payments.dto.order.InternalBookInfoResponse;
import com.nhnacademy.order_payments.dto.order.InternalBooksInfoResponse;
import com.nhnacademy.order_payments.dto.response.order.DeliveryAddressResponse;
import com.nhnacademy.order_payments.dto.response.order.OrderDetailResponse;
import com.nhnacademy.order_payments.dto.response.order.OrderListResponse;
import com.nhnacademy.order_payments.dto.response.order.OrderResponse;
import com.nhnacademy.order_payments.dto.review.BookOrderDetailRequest;
import com.nhnacademy.order_payments.dto.review.BookReviewRequest;
import com.nhnacademy.order_payments.dto.review.BookReviewResponse;
import com.nhnacademy.order_payments.entity.Delivery;
import com.nhnacademy.order_payments.entity.DeliveryDetail;
import com.nhnacademy.order_payments.entity.GuestOrderers;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.exception.IllegalReturnStateException;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.model.OrderDetailStatus;
import com.nhnacademy.order_payments.repository.GuestOrdererRepository;
import com.nhnacademy.order_payments.repository.OrderDetailRepository;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.saga.SagaOrchestrator;
import com.nhnacademy.order_payments.saga.event.OrderRefundEvent;
import com.nhnacademy.order_payments.service.packaging.PackagingService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        List<Order> orderList = orderRepository.findOrderByUserId(userId);  // 최신순 정렬 고려

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
        // 비회원은 주문 번호와 주문서 작성시 입력했던 비밀번호로 주문 조회
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

    // 배송지만 따로 DTO로 조회
    @Transactional(readOnly = true)
    public DeliveryAddressResponse getGuestDelivery(Long orderNumber, String password) {
        GuestOrderers guestInfo = guestOrdererRepository.findByOrder_OrderNumber(orderNumber)
                .orElseThrow(() -> {
                    log.warn("[OrderResultService] 비회원 - 찾을 수 없는 주문: {}", orderNumber);
                    return new NotFoundOrderException("[OrderResultService] 비회원 - 찾을 수 없는 주문");
                });

        if (!passwordEncoder.matches(password, guestInfo.getPassword())) {
            throw new RuntimeException("[OrderResultService] 비회원 - 일치하지 않는 비밀번호");
        }

        Delivery delivery = guestInfo.getOrder().getDelivery();
        if (delivery == null) {
            return new DeliveryAddressResponse(null, null, null, null);
        }

        LocalDate estimatedAt = extractEstimatedAt(delivery);

        return new DeliveryAddressResponse(
                delivery.getAddress(),
                delivery.getAddressDetail(),
                delivery.getPostalCode(),
                estimatedAt
        );
    }

    @Transactional(readOnly = true)
    public DeliveryAddressResponse getMemberDelivery(Long userId, Long orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundOrderException("[OrderResultService] 주문을 찾을 수 없습니다."));

        // 게스트 주문이거나, userId가 다르면 조회 불가
        if (order.getUserId() == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("[OrderResultService] 주문 조회 권한이 없습니다.");
        }

        Delivery delivery = order.getDelivery();
        if (delivery == null) {
            return new DeliveryAddressResponse(null, null, null, null);
        }

        LocalDate estimatedAt = extractEstimatedAt(delivery);

        return new DeliveryAddressResponse(
                delivery.getAddress(),
                delivery.getAddressDetail(),
                delivery.getPostalCode(),
                estimatedAt
        );
    }

    /**
     * Delivery에 매핑된 DeliveryDetailList에서 estimatedAt 최대값을 뽑아 반환
     * - 도착예정일이 여러 개(분리배송 등)일 수 있으니 "가장 늦은 날짜"를 대표값으로 사용
     */
    private LocalDate extractEstimatedAt(Delivery delivery) {
        if (delivery.getDeliveryDetailList() == null || delivery.getDeliveryDetailList().isEmpty()) {
            return null;
        }

        return delivery.getDeliveryDetailList().stream()
                .filter(Objects::nonNull)
                .map(DeliveryDetail::getEstimatedAt)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    private OrderResponse createOrderResponse(Order order) {
        List<OrderDetail> orderDetailList = order.getOrderDetailList();

        // 비회원(userId == null)은 리뷰조회 API(getBookReviewList)를 타면 안 됨
        // - book-review API는 userId 필요 -> 비회원이면 null이라 Book 서비스에서 NPE 발생
        // - 비회원은 책 정보만 /books/info 로 붙이고 reviewId는 null 처리
        if (order.getUserId() == null) {
            List<Long> bookIds = orderDetailList.stream()
                    .map(OrderDetail::getBookId)
                    .distinct()
                    .toList();

            InternalBooksInfoResponse booksInfoResponse = bookApiClient.getBookInfos(new BookApiRequest(bookIds));

            Map<Long, InternalBookInfoResponse> bookInfoMap =
                    (booksInfoResponse == null || booksInfoResponse.orderBookInfoRespDTOList() == null)
                            ? Map.of()
                            : booksInfoResponse.orderBookInfoRespDTOList().stream()
                            .collect(Collectors.toMap(
                                    b -> b.bookId(),   // long -> Long auto boxing
                                    b -> b,
                                    (a, b) -> a
                            ));

            List<OrderDetailResponse> detailResponses = orderDetailList.stream()
                    .map(od -> {
                        InternalBookInfoResponse bookInfo = bookInfoMap.get(od.getBookId());

                        String title = "정보 없음";
                        String imgUrl = null;

                        if (bookInfo != null) {
                            if (bookInfo.title() != null) title = bookInfo.title();
                            imgUrl = bookInfo.coverImage();
                        }

                        return new OrderDetailResponse(od.getId(),
                                od.getBookId(), title, imgUrl, od.getPrice(), od.getQuantity(),
                                packagingService.getPackagingName(od.getPackagingId()), od.getOrderDetailStatus(),
                                null // 비회원은 reviewId 없음
                        );
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
                    detailResponses
            );
        }

        // 기존 회원 로직 그대로 (리뷰조회 포함)
        BookReviewRequest reviewRequest = new BookReviewRequest(order.getUserId(),
                orderDetailList.stream()
                        .map(od -> new BookOrderDetailRequest(od.getBookId(), od.getId()))
                        .toList());

        Map<Long, BookReviewResponse> bookInfoMap = bookApiClient.getBookReviewList(reviewRequest).stream()
                .collect(Collectors.toMap(br -> br.book().bookId(), b -> b));

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
        /**
         *      SHIPPED,    // 배송 중
         *     DELIVERED,  // 배송 완료
         *     이 두개에 대해서만 반품 가능
         */
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId).orElseThrow();

        if(orderDetail.getOrderDetailStatus() != OrderDetailStatus.DELIVERED &&
                orderDetail.getOrderDetailStatus() != OrderDetailStatus.SHIPPED) {
            throw new IllegalReturnStateException("배송 중 또는 배송 완료 상태에서만 반품 신청이 가능합니다.");
        }

        Long refundAmount = orderDetail.getPrice() * orderDetail.getQuantity() - 3000;
        // ---> 배송비 제외하고 반품 가격 책정 (할인가 문제 해결해야함)
        OrderRefundEvent event = new OrderRefundEvent(orderDetail, refundAmount);

        sagaOrchestrator.start(event);

        orderDetail.setOrderDetailStatus(OrderDetailStatus.RETURNED);
    }
}
