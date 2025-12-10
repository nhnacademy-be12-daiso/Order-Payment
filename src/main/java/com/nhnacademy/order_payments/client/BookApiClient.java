package com.nhnacademy.order_payments.client;


import com.nhnacademy.order_payments.dto.cart.BookApiResponse;
import com.nhnacademy.order_payments.dto.cart.BookApiRequest;
import com.nhnacademy.order_payments.dto.order.BookInfoResponse;
import com.nhnacademy.order_payments.dto.review.BookReviewRequest;
import com.nhnacademy.order_payments.dto.review.BookReviewResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "TEAM3-BOOKSEARCH", path = "/api/books")
public interface BookApiClient {

    /**
     * 1. BookApiRequest 요청 - bookId로 이루어진 리스트<p>
     * 2. BookApiResponse 응답 - 요청 받은 bookId의 최신 정보(현재 제목, 현재 가격)의 리스트
     */
    // 장바구니용: 책에 대한 최소 정보만 받아옴 (제목, 가격)
    @PostMapping("/list")
    List<BookApiResponse> getBookList(@RequestBody BookApiRequest bookApiRequest);
    // Q. 받아오는건데 왜 GET이 아닌 POST요청을 보내는가?
    // A. 보낼때 Body에 데이터를 실어서 보내려면 Post요청으로 보내야함

    /**
     * 1. BookApiRequest 요청 - bookId로 이우러진 리스트<p>
     * 2. BookInfoResponse 응답 - BookInfo 리스트<p>
     * 2-1. BookInfo DTO - 요청 받은 bookId의 최신 정보(현재 제목, 현재 가격)
     */
    // 주문서 작성용: 책에 대한 자세한 정보를 받아옴 (정확성 요구함!!!)
    @PostMapping("/info")
    BookInfoResponse getBookInfos(@RequestBody BookApiRequest bookApiRequest);
    // -----> response dto 재탕해도 상관 없겠지?

    /**
     * 1. BookReviewRequest 요청 - userId(userCreatedId), BookOrderDetailRequest 리스트<p>
     * 1-2. BookOrderDetailRequest DTO - bookId와 orderDetailId<p>
     * 2. BookReviewResponse 응답 - 도서 정보, orderDetailId, reviewId
     * 2-2. BookResponse DTO - 요청 받은 bookId의 제목, 이미지 리스트(추후에 하나만 받는 것도 고려)
     */
    // 주문 내역 조회 페이지에서 보는 리뷰
    @PostMapping("/list/book-review")
    List<BookReviewResponse> getBookReviewList(@RequestBody BookReviewRequest bookReviewRequest);

}
