package com.nhnacademy.order_payments.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentRabbitMqConfig {

    // ------- payment 설정 ---------
    @Value("${rabbitmq.queue.payment}")
    private String PAYMENT_QUEUE;
    private static final String COUPON_EXCHANGE = "team3.coupon.exchange";
    private static final String ROUTING_KEY_USED = "coupon.used";

    // 구독할 Exchange
    @Bean
    public TopicExchange couponExchange() {
        return new TopicExchange(COUPON_EXCHANGE);
    }

    // 내가 받아볼 메세지 큐
    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true); // 서버 재시작해도 유지될지 여부
    }

    // 구독할 Exchange와 내 큐를 연결(바인딩) 하는 코드 ---> 실제 구독하는 느낌
    @Bean
    public Binding bindingCoupon(Queue paymentQueue, TopicExchange couponExchange) {
        return BindingBuilder.bind(paymentQueue)
                .to(couponExchange)
                .with(ROUTING_KEY_USED);
    }


}
