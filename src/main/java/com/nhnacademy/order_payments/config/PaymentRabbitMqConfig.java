package com.nhnacademy.order_payments.config;

import org.springframework.amqp.core.*;
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
        return QueueBuilder.durable(PAYMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", "team3.payment.dlx") // 큐에서 문제가 생기면 해당 DLX로 보냄
                .withArgument("x-dead-letter-routing-key", "fail.payment")
                .build();
    }

    // 구독할 Exchange와 내 큐를 연결(바인딩) 하는 코드 ---> 실제 구독하는 느낌
    @Bean
    public Binding bindingCoupon(Queue paymentQueue, TopicExchange couponExchange) {
        return BindingBuilder.bind(paymentQueue)
                .to(couponExchange)
                .with(ROUTING_KEY_USED);
    }


}
