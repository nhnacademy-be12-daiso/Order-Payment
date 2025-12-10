package com.nhnacademy.order_payments.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    private static final String ORDER_EXCHANGE = "order.exchange";
    // --> order에서 발행한 메세지들을 던질 exchange
    private static final String USER_QUEUE = "order.confirmed.member.queue";
    // ---> 구독할 전용 큐
    private static final String ROUTING_KEY_CONFIRMED = "order.confirmed";


    // 주문 로직을 수행하라는 메세지를 발송하는 Exchange
    @Bean
    public TopicExchange orderExchange() { // Topic은 패턴으로 바인딩하는거라 유연성 조음
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue userQueue() {
        return new Queue(USER_QUEUE, true); // 서버 재시작해도 유지될지 여부
    }

    @Bean
    public Binding bindingUserQueue(Queue userQueue, TopicExchange orderExchange) {
        // userQueue를 orderExchage에 order.confirmed 키로 연결

        return BindingBuilder.bind(userQueue)
                .to(orderExchange)
                .with(ROUTING_KEY_CONFIRMED);
    }


}
