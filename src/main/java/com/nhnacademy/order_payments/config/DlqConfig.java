package com.nhnacademy.order_payments.config;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/** DLQ 설정 페이지
 *  각 서비스에서 실패 시, 실패한 메시지를 한 곳에 모음
 */

@RequiredArgsConstructor
@Configuration
public class DlqConfig {


    @Value("${rabbitmq.queue.dlq}")
    private String SAGA_FAILURE_DLQ;

    @Bean
    public Queue sagaFailureDlq() {
        return new Queue(SAGA_FAILURE_DLQ);
    }

    @Bean
    public DirectExchange bookDlx() {
        return new DirectExchange("team3.book.dlx");
    }

    @Bean
    public DirectExchange userDlx() {
        return new DirectExchange("team3.user.dlx");
    }

    @Bean
    public DirectExchange couponDlx() {
        return new DirectExchange("team3.coupon.dlx");
    }

    @Bean
    public DirectExchange paymentDlx() {
        return new DirectExchange("team3.payment.dlx");
    }

    @Bean
    public Binding bookDlxToDlqBinding() {
        return BindingBuilder.bind(sagaFailureDlq())
                .to(bookDlx())
                .with("fail.book");
    }

    @Bean
    public Binding userDlxToDlqBinding() {
        return BindingBuilder.bind(sagaFailureDlq())
                .to(userDlx())
                .with("fail.user");
    }

    @Bean
    public Binding couponDlxToDlqBinding() {
        return BindingBuilder.bind(sagaFailureDlq())
                .to(couponDlx())
                .with("fail.coupon");
    }

    @Bean
    public Binding paymentDlxToDlqBinding() {
        return BindingBuilder.bind(sagaFailureDlq())
                .to(paymentDlx())
                .with("fail.payment");
    }
}