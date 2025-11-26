package com.nhnacademy.order_payments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.SecretKey;

@Configuration
@Profile("dev-toss")
public class TossClientConfig {

    @Value("${payments.toss.base-url}")
    private String baseUrl;

    @Value("${payments.toss.secret-key}")
    private String secretKey;

    @Bean
    public WebClient tossWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setBasicAuth(secretKey, "");
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();

    }

}
