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
import com.nhnacademy.order_payments.exception.NotFoundDeliveryException;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.repository.OrderDetailRepository;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.service.PackagingService;
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
                bookApiClient.getBookReviewList(new BookReviewRequest(order.getUserId(), orderDetailList.stream()
                                .map(od -> new BookOrderDetailRequest(od.getBookId(), od.getId()))
                                .toList())).stream()
                        .collect(Collectors.toMap(BookReviewResponse::orderDetailId, b -> b));

        Delivery delivery = order.getDelivery();
        if (delivery == null) {
            log.error("배송정보가 존재하지 않습니다 - 주문 번호:{}", order.getOrderNumber());
            throw new NotFoundDeliveryException("배송정보가 존재하지 않습니다.");
        }


        List<DeliveryDetail> deliveryDetailList = delivery.getDeliveryDetailList();
        List<DeliveryDetailResponse> deliveryDetailResponses = new ArrayList<>();
        if (deliveryDetailList == null || deliveryDetailList.isEmpty()) {
            log.warn("아직 배송상세가 존재하지 않아 임시 목록으로 표시합니다 - 주문 번호: {}", order.getOrderNumber());
            deliveryDetailResponses.add(new DeliveryDetailResponse(null, "출고 대기", null, null, null, null,
                    orderDetailList.stream()
                            .map(od ->
                                    new OrderDetailResponse(od.getId(), od.getBookId(),
                                            bookList.get(od.getId()).book().title(),
                                            !bookList.get(od.getId()).book().imageList().isEmpty() ?
                                                    bookList.get(od.getId()).book().imageList().getFirst().path() :
                                                    null,
                                            od.getPrice(), od.getQuantity(),
                                            packagingService.getPackagingName(od.getPackagingId()),
                                            od.getOrderDetailStatus(), bookList.get(od.getId()).reviewId()))
                            .toList()));

        } else {
            Map<Long, OrderDetail> orderDetailMap = orderDetailList.stream()
                    .collect(Collectors.toMap(OrderDetail::getId, od -> od));

//            for(DeliveryDetail dd: deliveryDetailList){
//
//                List<OrderDetailResponse> orderDetailResponseList = new ArrayList<>();
//                for(DeliveryOrderDetail dod :dd.getDeliveryOrderDetails()){
//                    OrderDetail orderDetail = orderDetailMap.get(dod.getOrderDetail().getId());
//                    BookReviewResponse bookReviewResponse = bookList.get(dod.getOrderDetail().getId());
//                    BookResponse book = bookReviewResponse.book();
//
//                    orderDetailResponseList.add(new OrderDetailResponse(orderDetail.getId(), book.title(), book.imageList().getFirst().path(), orderDetail.getPrice(),
//                            dod.getQuantity(), packagingService.getPackagingName(orderDetail.getPackagingId()), orderDetail.getOrderDetailStatus(), bookReviewResponse.reviewId()));
//                }
//
//                deliveryDetailResponses.add(new DeliveryDetailResponse(dd.getId(), dd.getDeliveryCompanyName(), dd.getDeliveryManName(),
//                        dd.getEstimatedAt(), dd.getCompleteAt(), dd.getDeliveryStatus(), orderDetailResponseList));
//            }

            // 같은 기능을 중첩 for문이 아니라 stream을 이용해 해당 로직 변경 - stream으로 이렇게 까지 사용을 하네..
            deliveryDetailResponses = deliveryDetailList.stream()
                    .map(dd -> {
                        List<OrderDetailResponse> orderDetailResponseList = dd.getDeliveryOrderDetails().stream()
                                .map(dod -> {
                                    OrderDetail orderDetail = orderDetailMap.get(dod.getOrderDetail().getId());
                                    BookReviewResponse br = bookList.get(orderDetail.getId());

                                    return new OrderDetailResponse(orderDetail.getId(), orderDetail.getBookId(),
                                            br.book().title(),
                                            !br.book().imageList().isEmpty() ? br.book().imageList().getFirst().path() :
                                                    null,
                                            orderDetail.getPrice(), dod.getQuantity(),
                                            orderDetail.getPackagingId() != null ?
                                                    packagingService.getPackagingName(orderDetail.getPackagingId()) :
                                                    null,
                                            orderDetail.getOrderDetailStatus(), br.reviewId());
                                })
                                .toList();

                        return new DeliveryDetailResponse(dd.getId(), dd.getDeliveryCompanyName(),
                                dd.getDeliveryManName(),
                                dd.getEstimatedAt(), dd.getCompleteAt(), dd.getDeliveryStatus(),
                                orderDetailResponseList);
                    })
                    .toList();
        }

        DeliveryResponse deliveryResponse =
                new DeliveryResponse(delivery.getId(), delivery.getAddress(), delivery.getAddressDetail(),
                        delivery.getPostalCode(),
                        delivery.getReceiverName(), delivery.getReceiverPhoneNumber(), delivery.getFee(),
                        deliveryDetailResponses);

        return new OrderResponse(order.getId(), order.getOrderNumber(), order.getOrderStatus(), order.getOrderDate(),
                order.getOrdererName(), order.getTotalPrice(), order.getPhoneNumber(), order.getEmail(),
                order.getGrade(),
                deliveryResponse);
    }

    private OrderListResponse createOrderListResponse(List<Order> orderList) {
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (Order o : orderList) {
            orderResponseList.add(createOrderResponse(o));
        }
        return new OrderListResponse(orderResponseList);
    }
}
