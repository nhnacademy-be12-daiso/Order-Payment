package com.nhnacademy.order_payments.infra;

import com.nhnacademy.order_payments.dto.BookDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class BookApiClientImpl implements BookApiClient {

    @Value("${book.api.url")
    private String bookApiUrl;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(bookApiUrl)
                .build();
    }

    @Override
    public BookDto getBookInfo(Long bookId) {

        return webClient.get()
                .uri("/api/books/{bookId}", bookId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        Mono.error(new RuntimeException("책 API 통신 오류 : " + clientResponse)))
                .bodyToMono(BookDto.class)
                .block(); // 동기 통신 ----> 나중에 비동기로 바꿔야 함
    }
}
