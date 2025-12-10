package com.nhnacademy.order_payments.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderEventPublisher {

    private final AmqpTemplate rabbitTemplate;

//    private static final String


}
