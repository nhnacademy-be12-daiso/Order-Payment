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

import com.nhnacademy.order_payments.dto.order.BookSummaryDto;
import com.nhnacademy.order_payments.dto.order.DeliverySummaryDto;
import com.nhnacademy.order_payments.dto.order.OrderSummaryDto;
import com.nhnacademy.order_payments.entity.Delivery;
import com.nhnacademy.order_payments.entity.DeliveryDetail;
import com.nhnacademy.order_payments.entity.GuestOrderers;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.repository.DeliveryDetailRepository;
import com.nhnacademy.order_payments.repository.DeliveryRepository;
import com.nhnacademy.order_payments.repository.GuestOrdererRepository;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.saga.SagaOrchestrator;
import com.nhnacademy.order_payments.saga.event.OrderConfirmedEvent;
import com.nhnacademy.order_payments.saga.order.OrderEventFactory;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final GuestOrdererRepository guestOrdererRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryDetailRepository deliveryDetailRepository;

    private final OrderEventFactory orderEventFactory;
    private final OrderValidationService orderValidationService;
    private final PasswordEncoder passwordEncoder;
    private final SagaOrchestrator sagaOrchestrator;

    @Transactional
    public Order precessOrderPayment(Long userId, OrderSummaryDto dto) {

        // TODO 주문 검증 및 Order DB에 임시 주문 정보 저장
        Order order = createOrder(userId, dto);
        OrderConfirmedEvent event = orderEventFactory.create(userId, order, dto);

        if (!orderValidationService.validateOrder(userId, dto)) { // 검증 실패
            throw new RuntimeException("주문 정보에 대한 검증 실패"); // -----> 예외처리 다시 해주기 <<<<<<<<
        }

        // SagaOrchestrator 주입받아서 saga 시작
        sagaOrchestrator.start(event);

        return order; // 임시로 뱉어내는 로직
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

        // 3-1. 배송지 저장 (주문 당시 배송지)
        DeliverySummaryDto deliveryDto = dto.deliverySummaryDto();
        if (deliveryDto != null) {
            String address = (deliveryDto.deliveryAddress() == null) ? "" : deliveryDto.deliveryAddress();
            String detail = (deliveryDto.deliveryAddressDetail() == null) ? "" : deliveryDto.deliveryAddressDetail();

            Delivery delivery = new Delivery(
                    address,
                    deliveryDto.postalCode(),
                    deliveryDto.receiverName(),
                    deliveryDto.receiverPhoneNumber(),
                    deliveryDto.DeliveryFee()
            );

            delivery.setAddressDetail(detail);
            delivery.setOrder(savedOrder);

            // 양방향 세팅
            savedOrder.setDelivery(delivery);

            // Delivery 먼저 저장
            deliveryRepository.save(delivery);

            // ✅ 추가: 주문서에서 선택한 "도착예정일(배송일 선택)"을 DeliveryDetail.estimatedAt 으로 저장
            String deliveryDate = deliveryDto.deliveryDate(); // 프론트에서 yyyy-MM-dd 형태로 넘어온다고 가정
            LocalDate estimatedAt = parseEstimatedAt(deliveryDate);

            if (estimatedAt != null) {
                DeliveryDetail deliveryDetail = new DeliveryDetail();
                deliveryDetail.setDelivery(delivery);
                deliveryDetail.setEstimatedAt(estimatedAt);

                // (배송사/배송기사/상태/완료시간은 아직 없으니 미세팅)
                deliveryDetailRepository.save(deliveryDetail);
            }
        }

        // 4. 비회원일 경우 인증 정보(비밀번호) 저장
        if (userId == null) {
            String password = passwordEncoder.encode(dto.ordererSummaryDto().ordererPassword());    // 비밀번호 암호화

            GuestOrderers guestOrderers = new GuestOrderers(savedOrder, password);
            guestOrdererRepository.save(guestOrderers);
        }

        return savedOrder;
    }

    /**
     * deliveryDate(String)를 LocalDate로 파싱
     * - 기본 기대 포맷: yyyy-MM-dd
     * - 혹시라도 "yyyy-MM-ddTHH:mm:ss" 같은 값이 들어오면 앞 10자리만 사용
     */
    private LocalDate parseEstimatedAt(String deliveryDate) {
        if (deliveryDate == null || deliveryDate.isBlank()) {
            return null;
        }

        String value = deliveryDate.trim();
        try {
            // "2025-01-03T00:00:00" 같은 경우 대비
            if (value.length() >= 10) {
                value = value.substring(0, 10);
            }
            return LocalDate.parse(value);
        } catch (Exception e) {
            log.warn("[주문생성] deliveryDate 파싱 실패. value={}", deliveryDate, e);
            return null;
        }
    }
}
