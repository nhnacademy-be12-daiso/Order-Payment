package com.nhnacademy.order_payments.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class TossClientConfig {

    @Value("${payment.base-url}")
    private String baseUrl;

    @Value("${payment.secret-key}")
    private String secretKey;

    @Bean
    public WebClient tossWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)   // TCP 연결 타임아웃 5초
                .responseTimeout(Duration.ofSeconds(10));               // 응답 대기 타임아웃 10초

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeaders(headers -> {
                    headers.setBasicAuth(secretKey, "");
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }

}
