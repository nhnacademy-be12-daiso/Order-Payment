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
import com.nhnacademy.order_payments.dto.response.order.DeliveryDetailResponse;
import com.nhnacademy.order_payments.dto.response.order.DeliveryResponse;
import com.nhnacademy.order_payments.dto.response.order.OrderDetailResponse;
import com.nhnacademy.order_payments.dto.response.order.OrderListResponse;
import com.nhnacademy.order_payments.dto.response.order.OrderResponse;
import com.nhnacademy.order_payments.dto.review.BookOrderDetailRequest;
import com.nhnacademy.order_payments.dto.review.BookReviewRequest;
import com.nhnacademy.order_payments.dto.review.BookReviewResponse;
import com.nhnacademy.order_payments.entity.Delivery;
import com.nhnacademy.order_payments.entity.DeliveryDetail;
import com.nhnacademy.order_payments.entity.GuestOrderer;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.repository.OrderDetailRepository;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.service.packaging.PackagingService;
import java.util.ArrayList;
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
    private final OrderDetailRepository orderDetailRepository;

    private final GuestOrdererService guestOrdererService;
    private final PackagingService packagingService;

    private final BookApiClient bookApiClient;

    @Transactional
    public OrderListResponse getOrderList(Long userId) {
        GuestOrderer guestOrderer = guestOrdererService.getOrderer(userId);

        List<Order> orderList = null;
        if (guestOrderer == null) {
            orderList = orderRepository.findOrderByUserId(userId);
        } else {
            orderList = guestOrderer.getOrderList();
        }

        if (orderList == null || orderList.isEmpty()) {
            log.warn("해당 회원의 주문 리스트를 찾지 못했습니다 - userId:{}", userId);
            return new OrderListResponse(List.of());
        }

        return createOrderListResponse(orderList);
    }

    private OrderResponse createOrderResponse(Order order) {

        List<OrderDetail> orderDetailList = order.getOrderDetailList();

        if (orderDetailList == null || orderDetailList.isEmpty()) {
            log.error("해당 주문의 주문상세가 존재하지 않습니다 - 주문 번호:{}", order.getOrderNumber());
            throw new NotFoundOrderException("주문상세가 존재하지 않습니다");
        }

        Map<Long, BookReviewResponse> bookList =
                bookApiClient.getBookReviewList(
                                new BookReviewRequest(
                                        order.getUserId(),
                                        orderDetailList.stream()
                                                .map(od -> new BookOrderDetailRequest(od.getBookId(), od.getId()))
                                                .toList()
                                )
                        ).stream()
                        .collect(Collectors.toMap(BookReviewResponse::orderDetailId, b -> b));

        Delivery delivery = order.getDelivery();

        // 배송정보가 없을 수 있으므로 deliveryResponse를 기본 null로 둔다.
        DeliveryResponse deliveryResponse = null;

        if (delivery == null) {
            // [MOD] 기존에는 예외를 던져서 주문목록 전체가 500으로 죽었음.
            //       주문목록에서는 배송정보가 아직 없을 수 있으니 null로 내려준다.
            log.warn("배송정보가 아직 없습니다(주문목록에서는 delivery=null) - 주문 번호:{}", order.getOrderNumber());
        } else {
            // [MOVE] 배송 관련 로직은 delivery가 있을 때만 수행
            List<DeliveryDetail> deliveryDetailList = delivery.getDeliveryDetailList();
            List<DeliveryDetailResponse> deliveryDetailResponses = new ArrayList<>();

            if (deliveryDetailList == null || deliveryDetailList.isEmpty()) {
                log.warn("아직 배송상세가 존재하지 않아 임시 목록으로 표시합니다 - 주문 번호: {}", order.getOrderNumber());
                deliveryDetailResponses.add(new DeliveryDetailResponse(
                        null, "출고 대기", null, null, null, null,
                        orderDetailList.stream()
                                .map(od -> new OrderDetailResponse(
                                        od.getId(),
                                        od.getBookId(),
                                        bookList.get(od.getId()).book().title(),
                                        !bookList.get(od.getId()).book().imageList().isEmpty()
                                                ? bookList.get(od.getId()).book().imageList().getFirst().path()
                                                : null,
                                        od.getPrice(),
                                        od.getQuantity(),
                                        packagingService.getPackagingName(od.getPackagingId()),
                                        od.getOrderDetailStatus(),
                                        bookList.get(od.getId()).reviewId()
                                ))
                                .toList()
                ));
            } else {
                Map<Long, OrderDetail> orderDetailMap = orderDetailList.stream()
                        .collect(Collectors.toMap(OrderDetail::getId, od -> od));

                deliveryDetailResponses = deliveryDetailList.stream()
                        .map(dd -> {
                            List<OrderDetailResponse> orderDetailResponseList = dd.getDeliveryOrderDetails().stream()
                                    .map(dod -> {
                                        OrderDetail orderDetail = orderDetailMap.get(dod.getOrderDetail().getId());
                                        BookReviewResponse br = bookList.get(orderDetail.getId());

                                        return new OrderDetailResponse(
                                                orderDetail.getId(),
                                                orderDetail.getBookId(),
                                                br.book().title(),
                                                !br.book().imageList().isEmpty()
                                                        ? br.book().imageList().getFirst().path()
                                                        : null,
                                                orderDetail.getPrice(),
                                                dod.getQuantity(),
                                                orderDetail.getPackagingId() != null
                                                        ? packagingService.getPackagingName(orderDetail.getPackagingId())
                                                        : null,
                                                orderDetail.getOrderDetailStatus(),
                                                br.reviewId()
                                        );
                                    })
                                    .toList();

                            return new DeliveryDetailResponse(
                                    dd.getId(),
                                    dd.getDeliveryCompanyName(),
                                    dd.getDeliveryManName(),
                                    dd.getEstimatedAt(),
                                    dd.getCompleteAt(),
                                    dd.getDeliveryStatus(),
                                    orderDetailResponseList
                            );
                        })
                        .toList();
            }

            // [MOVE] deliveryResponse 생성도 delivery != null 일 때만 수행
            deliveryResponse = new DeliveryResponse(
                    delivery.getId(),
                    delivery.getAddress(),
                    delivery.getAddressDetail(),
                    delivery.getPostalCode(),
                    delivery.getReceiverName(),
                    delivery.getReceiverPhoneNumber(),
                    delivery.getFee(),
                    deliveryDetailResponses
            );
        }

        // [MOD] deliveryResponse는 없으면 null 그대로 내려감
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderStatus(),
                order.getOrderDate(),
                order.getOrdererName(),
                order.getTotalPrice(),
                order.getPhoneNumber(),
                order.getEmail(),
                deliveryResponse
        );
    }

    private OrderListResponse createOrderListResponse(List<Order> orderList) {
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (Order o : orderList) {
            orderResponseList.add(createOrderResponse(o));
        }
        return new OrderListResponse(orderResponseList);
    }
}