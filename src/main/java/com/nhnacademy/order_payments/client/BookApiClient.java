package com.nhnacademy.order_payments.client;


import com.nhnacademy.order_payments.dto.BookApiResponse;
import com.nhnacademy.order_payments.dto.BookApiRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "TEAM3-BOOKSEARCH", path = "/api/books")
public interface BookApiClient {

    @PostMapping("/list")
    List<BookApiResponse> getBookList(@RequestBody BookApiRequest bookApiRequest);
}
