package com.nhnacademy.order_payments.client;


import com.nhnacademy.order_payments.dto.cart.BookApiResponse;
import com.nhnacademy.order_payments.dto.cart.BookApiRequest;
import com.nhnacademy.order_payments.dto.order.BookInfo;
import com.nhnacademy.order_payments.dto.order.BookInfoResponse;
import com.nhnacademy.order_payments.dto.*;
import com.nhnacademy.order_payments.dto.review.BookReviewRequest;
import com.nhnacademy.order_payments.dto.review.BookReviewResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "TEAM3-BOOKSEARCH", path = "/api/books")
public interface BookApiClient {

    // 책에 대한 최소 정보만 받아옴 (장바구니용)
    // 제목, 가격
    @PostMapping("/list")
    List<BookApiResponse> getBookList(@RequestBody BookApiRequest bookApiRequest);
    // Q. 받아오는건데 왜 GET이 아닌 POST요청을 보내는가?
    // A. 보낼때 Body에 데이터를 실어서 보내려면 Post요청으로 보내야함

    // 책에 대한 자세한 정보를 받아옴 (주문서 작성용) ----> 정확성 요구함!!!
    @PostMapping("/info")
    BookInfoResponse getBookInfos(@RequestBody BookApiRequest bookApiRequest);
    // -----> response dto 재탕해도 상관 없겠지?

    @PostMapping("/list/book-review")
    List<BookReviewResponse> getBookReviewList(@RequestBody BookReviewRequest bookReviewRequest);
}
