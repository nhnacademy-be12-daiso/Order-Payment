package com.nhnacademy.order_payments.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    // 주문 Saga 시작 세팅
    private static final String ORDER_EXCHANGE = "team3.order.exchange";
    private static final String ORDER_QUEUE = "team3.payment.complete.order.queue";
    private static final String PAYMENT_EXCHANGE = "team3.payment.exchange";
    private static final String ROUTING_KEY_COMPLETE = "payment.success";
    // ---> 라우팅 키

    // 구독할 Exchange
    @Bean
    public TopicExchange paymentExchange() {
        // Payment API에 정의된 Exchange 이름을 그대로 사용합니다.
        // 이 선언으로 Spring은 이 Exchange를 컨테이너에 등록하고,
        // RabbitMQ 서버에 이미 존재하는지 확인 후 참조합니다.
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    // 내가 받아볼 메세지 큐
    @Bean
    public Queue OrderCompletionQueue() {
        return new Queue(ORDER_QUEUE, true); // 서버 재시작해도 유지될지 여부
    }

    // 구독할 Exchange와 내 큐를 연결(바인딩) 하는 코드 ---> 실제 구독하는 느낌
    @Bean
    public Binding bindingOrderCompletion(Queue orderCompletionQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(orderCompletionQueue)
                .to(paymentExchange)
                .with(ROUTING_KEY_COMPLETE);
    }

    // 내가 발행할 메세지를 보낼 Exchange
    // 받는 쪽에서는 이 모양 그대로 정의해놔야함
    @Bean
    public TopicExchange orderExchange() { // Topic은 패턴으로 바인딩하는거라 유연성 조음
        return new TopicExchange(ORDER_EXCHANGE);
    }
}
