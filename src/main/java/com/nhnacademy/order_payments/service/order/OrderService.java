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
import com.nhnacademy.order_payments.entity.GuestOrderers;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.entity.OrderOutbox;
import com.nhnacademy.order_payments.exception.FailedSerializationException;
import com.nhnacademy.order_payments.repository.GuestOrdererRepository;
import com.nhnacademy.order_payments.repository.OrderOutboxRepository;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.saga.common.OrderConfirmedEvent;
import com.nhnacademy.order_payments.saga.order.OrderEventFactory;
import com.nhnacademy.order_payments.saga.order.OrderOutboxCommittedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    //    private final OrderEventPublisher eventPublisher;
    private final OrderRepository orderRepository;
    private final GuestOrdererRepository guestOrdererRepository;

    private final OrderEventFactory orderEventFactory;
    private final ObjectMapper objectMapper;
    private final OrderOutboxRepository orderOutboxRepository;
    private final ApplicationEventPublisher publisher;
    private final OrderValidationService orderValidationService;

    private final PasswordEncoder passwordEncoder;

    @Value("${rabbitmq.routing.confirmed}")
    private String routingKey;

    @Transactional
    public Order precessOrderPayment(Long userId, OrderSummaryDto dto) {

        // TODO 주문 검증 및 Order DB에 임시 주문 정보 저장
        // ---> saga와는 무관한 로컬 트랜잭션임

        if (!orderValidationService.validateOrder(userId, dto)) { // 검증 실패
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


    public Order createOrder(Long userId, OrderSummaryDto dto) {
        // 1. 주문 객체 생성 (아직 저장 안 함 - 비영속 상태) 및 기본 정보 세팅
        Order order = new Order(dto);
        order.setUserId(userId);    // userId == null일 때 (비회원일 때) 알아서 null 드감

        // 2. 상세 내역 조립
        for (BookSummaryDto book : dto.bookList()) {
            OrderDetail detail = new OrderDetail(book);

            // 주문-상세 연관관계 맺기
            order.addOrderDetail(detail);
        }

        // 3. 일단 주문부터 DB에 저장 (order_number 확정)
        Order savedOrder = orderRepository.save(order);

        // 4. 비회원일 경우 인증 정보(비밀번호) 저장
        if (userId == null) {
            String password = passwordEncoder.encode(dto.ordererSummaryDto().ordererPassword());    // 비밀번호 암호화

            GuestOrderers guestOrderers = new GuestOrderers(savedOrder, password);
            guestOrdererRepository.save(guestOrderers);
        }

        return savedOrder;
    }

}
