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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.order_payments.dto.order.BookSummaryDto;
import com.nhnacademy.order_payments.dto.order.OrderSummaryDto;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.entity.OrderOutbox;
import com.nhnacademy.order_payments.exception.FailedSerializationException;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.repository.OrderOutboxRepository;
import com.nhnacademy.order_payments.saga.common.OrderConfirmedEvent;
import com.nhnacademy.order_payments.saga.order.OrderEventFactory;
import com.nhnacademy.order_payments.saga.order.OrderOutboxCommittedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

//    private final OrderEventPublisher eventPublisher;
    private final OrderRepository orderRepository;
    private final OrderEventFactory orderEventFactory;
    private final ObjectMapper objectMapper;
    private final OrderOutboxRepository orderOutboxRepository;
    private final ApplicationEventPublisher publisher;


    @Value("${rabbitmq.routing.confirmed}")
    private String routingKey;

    @Transactional
    public Order precessOrderPayment(Long userId, OrderSummaryDto dto) {

        // TODO 주문 검증 및 Order DB에 임시 주문 정보 저장
        // ---> saga와는 무관한 로컬 트랜잭션임

        if (!validateOrder(userId, dto)) { // 검증 실패
            throw new RuntimeException("주문 정보에 대한 검증 실패"); // -----> 예외처리 다시 해주기 <<<<<<<<
        }

        // ----- 검증 이후 로직 -----

        // 일단 임시 데이터(아직 트랜잭션이 안돈 상태)를 DB에 저장함
        Order order = createOrder(userId, dto);
        OrderConfirmedEvent event = orderEventFactory.create(userId, order, dto);

        try {
            OrderOutbox outbox = new OrderOutbox(
                    order.getId(),
                    "ORDER",
                    "team3.saga.order.exchange",
                    routingKey,
                    objectMapper.writeValueAsString(event)
            );

            orderOutboxRepository.save(outbox);
            publisher.publishEvent(new OrderOutboxCommittedEvent(this, outbox.getId()));
            // ---> Outbox 저장 완료 후, 알림 이벤트 발행

        } catch (JsonProcessingException e) {
            log.error("객체 직렬화에 실패했습니다.");
            throw new FailedSerializationException("Failed to serialize event payload", e);
        }

//        eventPublisher.publishOrderConfirmedEvent(event);
        // ----> Outbox 패턴 도입으로 인해 없어도 됨
        return order; //임시
    }

    /**
     * Order 정보 검증하는 메서드
     */
    private boolean validateOrder(Long userId, OrderSummaryDto dto) { // boolean으로 반환하는게 과연 맞는지?
        return true;
    }

    public Order createOrder(Long userId, OrderSummaryDto dto) {
        // 1. 주문 객체 생성 (아직 저장 안 함 - 비영속 상태)
        Order order = new Order(dto);

        order.setUserId(userId);

        // 2. 상세 내역 조립
        for (BookSummaryDto book : dto.bookList()) {
            OrderDetail detail = new OrderDetail(book);

            // 주문-상세 연관관계 맺기
            order.addOrderDetail(detail);
        }

        // 3. 마지막에 한 번만 저장
        return orderRepository.save(order);
        // 부모를 저장하면 자식들이 Cascade에 의해 자동으로 저장됨
    }
}
