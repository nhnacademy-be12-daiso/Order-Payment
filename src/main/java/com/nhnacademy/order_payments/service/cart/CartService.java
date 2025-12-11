/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2025. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.order_payments.service.cart;

import com.nhnacademy.order_payments.client.BookApiClient;
import com.nhnacademy.order_payments.dto.cart.BookApiRequest;
import com.nhnacademy.order_payments.dto.cart.BookApiResponse;
import com.nhnacademy.order_payments.dto.cart.BookItem;
import com.nhnacademy.order_payments.entity.Cart;
import com.nhnacademy.order_payments.entity.CartDetail;
import com.nhnacademy.order_payments.repository.CartRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService { // 네이밍이 썩 맘에 들지 않음;

    private final BookApiClient bookApiClient;
    private final CartRepository cartRepository;

    /**
     * 1. 일단 DB에서 책번호 리스트 뽑아옴<p>
     * 2. 그거 리스트 뭉텅이를 Book API에 뽑아줌<p>
     * 3. 그 답장을 BookDto 리스트에 담아서 받아옴<p>
     * 4. 그거 답장해주면 됨
     */
    public List<BookItem> getCartList(Long userId) {
        // 사용자의 장바구니 뽑아옴
        Cart cart = cartRepository.findCartWithDetailsByUserId(userId).orElseGet(
                () -> {
                    Cart newCart = new Cart(userId);
                    log.info("장바구니 생성 : {}", userId);
                    return cartRepository.save(newCart);
                }
        );

        // 항목들 분리
        List<CartDetail> cartDetails = cart.getDetails();
        List<Long> bookIds = new ArrayList<>();

        // 항목 중에서 ID만 분리
        for (CartDetail cartDetail : cartDetails) {
            bookIds.add(cartDetail.getBookId());
        }
        BookApiRequest bookApiRequest = new BookApiRequest(bookIds); // DTO 작성

        // Book API에 책에 대한 정보 요청
        List<BookApiResponse> books = bookApiClient.getBookList(bookApiRequest);

        // 다시 매칭해서 응답 DTO 만들어주기
        List<BookItem> bookItemList = new ArrayList<>();
        for (CartDetail cartDetail : cartDetails) {
            for (BookApiResponse book : books) {
                if (cartDetail.getBookId() == book.bookId()) {
                    BookItem bookItem =
                            new BookItem(book.bookId(), book.title(), book.price(), cartDetail.getQuantity());
                    bookItemList.add(bookItem);
                }
            }
        }

        return bookItemList;
    }
}
