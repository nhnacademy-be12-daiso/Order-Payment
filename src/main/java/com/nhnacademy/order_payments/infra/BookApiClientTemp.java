package com.nhnacademy.order_payments.infra;

import com.nhnacademy.order_payments.dto.BookDto;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 도서 도메인과 통신하는 대신에 로컬에서 테스트하기 위함
 */
@Service
//@Qualifier("tempBookApi")
@Primary
public class BookApiClientTemp implements BookApiClient {

    @Override
    public BookDto getBookInfo(Long bookId) {

        int price = ThreadLocalRandom.current().nextInt(10, 31) * 1000;

        return new BookDto("Book" + bookId.toString(), price);
    }
}
