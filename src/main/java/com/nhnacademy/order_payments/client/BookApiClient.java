package com.nhnacademy.order_payments.client;


import com.nhnacademy.order_payments.dto.*;
import com.nhnacademy.order_payments.dto.review.BookReviewRequest;
import com.nhnacademy.order_payments.dto.review.BookReviewResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "TEAM3-BOOKSEARCH", path = "/api/books")
public interface BookApiClient {

    @PostMapping("/list")
    List<BookApiResponse> getBookList(@RequestBody BookApiRequest bookApiRequest);

    @PostMapping("/list/book-review")
    List<BookReviewResponse> getBookReviewList(@RequestBody BookReviewRequest bookReviewRequest);
}
