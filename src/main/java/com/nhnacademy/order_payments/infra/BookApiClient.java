package com.nhnacademy.order_payments.infra;

import com.nhnacademy.order_payments.dto.BookDto;

public interface BookApiClient {
    BookDto getBookInfo(Long bookId);
}
