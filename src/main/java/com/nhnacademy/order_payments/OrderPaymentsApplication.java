package com.nhnacademy.order_payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OrderPaymentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderPaymentsApplication.class, args);
    }

}
