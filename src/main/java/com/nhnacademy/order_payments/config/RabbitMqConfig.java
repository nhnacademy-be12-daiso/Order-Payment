package com.nhnacademy.order_payments.config;

import com.nhnacademy.order_payments.saga.common.SagaTopic;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OrderAPI의 RabbitMQ 설정
 * Payment -> Order의 바인딩 설정
 */
@Configuration
public class RabbitMqConfig {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @PostConstruct
    public void init() {
        boolean isDev = "dev".equalsIgnoreCase(activeProfile);
        SagaTopic.setMode(isDev);
    }

    @Bean("Saga")
    public Map<String, SagaTopic> sagaTopics() {
        return Arrays.stream(SagaTopic.values())
                .collect(Collectors.toMap(Enum::name, topic -> topic));
    }

    // 주문 Saga 시작 세팅
    private static final String ORDER_EXCHANGE = "team3.saga.order.exchange";
    @Value("${rabbitmq.queue.order}")
    private String ORDER_QUEUE;
    private static final String PAYMENT_EXCHANGE = "team3.saga.payment.exchange";
//    private static final String ROUTING_KEY_COMPLETE = "payment.success";
    @Value("${rabbitmq.routing.success}")
    private String ROUTING_KEY_COMPLETE;
    // ---> 라우팅 키

    // 구독할 Exchange
    @Bean
    public DirectExchange paymentExchange() {
        // Payment API에 정의된 Exchange 이름을 그대로 사용합니다.
        // 이 선언으로 Spring은 이 Exchange를 컨테이너에 등록하고,
        // RabbitMQ 서버에 이미 존재하는지 확인 후 참조합니다.
        return new DirectExchange(PAYMENT_EXCHANGE);
    }

    // 내가 받아볼 메세지 큐
    @Bean
    public Queue orderCompletionQueue() {
//        return new Queue(ORDER_QUEUE, true); // 서버 재시작해도 유지될지 여부
        return QueueBuilder.durable(ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", "team3.order.dlx") // 큐에서 문제가 생기면 해당 DLX로 보냄
                .withArgument("x-dead-letter-routing-key", "fail.order")
                .build();
        // ----> 여기도 DLQ 처리 해놔야 하나?
    }

    // 구독할 Exchange와 내 큐를 연결(바인딩) 하는 코드 ---> 실제 구독하는 느낌
    @Bean
    public Binding bindingOrderCompletion(Queue orderCompletionQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(orderCompletionQueue)
                .to(paymentExchange)
                .with(ROUTING_KEY_COMPLETE);
    }

    // 내가 발행할 메세지를 보낼 Exchange
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    // 수신용 컨버터
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    // 발신용 컨버터
    @Bean
    public MessageConverter simpleMessageConverter() {
        // String, byte[], Serializable 객체를 처리하는 기본 컨버터
        return new SimpleMessageConverter();
    }

    /**
     * 3. RabbitTemplate 설정
     * 위에서 만든 JSON 변환기를 템플릿에 끼워줍니다.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setMessageConverter(simpleMessageConverter());

        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter()); // 수신은 JSON 컨버터로!
        return factory;
    }
}
