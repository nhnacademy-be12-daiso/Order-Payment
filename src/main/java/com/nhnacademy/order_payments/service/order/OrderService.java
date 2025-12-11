package com.nhnacademy.order_payments.service.order;

import com.nhnacademy.order_payments.dto.order.BookSummaryDto;
import com.nhnacademy.order_payments.dto.order.OrderSummaryDto;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.event.OrderConfirmedEvent;
import com.nhnacademy.order_payments.event.OrderEventPublisher;
import com.nhnacademy.order_payments.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderEventPublisher eventPublisher;
    private final OrderRepository orderRepository;

    @Transactional
    public void precessOrderPayment(Long userId, OrderSummaryDto dto) {

        // TODO 주문 검증 및 Order DB에 임시 주문 정보 저장
        // ---> saga와는 무관한 로컬 트랜잭션임

        if(!validateOrder(userId, dto)) { // 검증 실패
            throw new RuntimeException("주문 정보에 대한 검증 실패"); // -----> 예외처리 다시 해주기 <<<<<<<<
        }


        // ----- 검증 이후 로직 -----

        // 일단 임시 데이터(아직 트랜잭션이 안돈 상태)를 DB에 저장함
        Order order = createOrder(dto);

        // 이벤트 객체 생성
        OrderConfirmedEvent event = new OrderConfirmedEvent(userId, order, dto);

        eventPublisher.publishOrderConfirmedEvent(event);
    }

    /**
     * Order 정보 검증하는 메서드
     */
    private boolean validateOrder(Long userId, OrderSummaryDto dto) { // boolean으로 반환하는게 과연 맞는지?
        return true;
    }

    public Order createOrder(OrderSummaryDto dto) {

        Order order = orderRepository.save(new Order(dto));
        for(BookSummaryDto book : dto.bookList()) {
            OrderDetail detail = new OrderDetail(book);
            order.addOrderDetail(detail);
        }

        return orderRepository.save(order);
        // 부모를 저장하면 자식들이 Cascade에 의해 자동으로 저장됨
    }
}
