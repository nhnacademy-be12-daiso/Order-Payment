package com.nhnacademy.order_payments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("dev-toss")
public class TossClientConfig {

    @Value("${payment.base-url}")
    private String baseUrl;

    @Value("${payment.secret-key}")
    private String secretKey;

    @Bean
    public WebClient tossWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    //시크릿 키를 base64로 인코딩
                    headers.setBasicAuth(secretKey, "");
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();

    }

}
