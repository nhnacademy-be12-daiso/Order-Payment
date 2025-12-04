package com.nhnacademy.order_payments.service.cart;

import com.nhnacademy.order_payments.dto.cart.BookApiRequest;
import com.nhnacademy.order_payments.dto.cart.BookApiResponse;
import com.nhnacademy.order_payments.dto.cart.BookItem;
import com.nhnacademy.order_payments.entity.Cart;
import com.nhnacademy.order_payments.entity.CartDetail;
import com.nhnacademy.order_payments.exception.NotFoundUserCartException;
import com.nhnacademy.order_payments.client.BookApiClient;
import com.nhnacademy.order_payments.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService { // 네이밍이 썩 맘에 들지 않음;

    private final BookApiClient bookApiClient;
    private final CartRepository cartRepository;

    public List<BookItem> getCartList(Long userId) {

        /**
         * 1. 일단 DB에서 책번호 리스트 뽑아옴
         * 2. 그거 리스트 뭉텅이를 Book API에 뽑아줌
         * 3. 그 답장을 BookDto 리스트에 담아서 받아옴
         * 4. 그거 답장해주면 됨
         */
        // 사용자의 장바구니 뽑아옴
        Cart cart = cartRepository.findCartWithDetailsByUserId(userId).orElseThrow(() -> new NotFoundUserCartException(userId));

        // 항목들 분리
        List<CartDetail> cartDetails = cart.getDetails();
        List<Long> bookIds = new ArrayList<>();

        // 항목 중에서 ID만 분리
        for(CartDetail cartDetail : cartDetails) {
            bookIds.add(cartDetail.getBookId());
        }
        BookApiRequest bookApiRequest = new BookApiRequest(bookIds); // DTO 작성

        // Book API에 책에 대한 정보 요청
        List<BookApiResponse> books = bookApiClient.getBookList(bookApiRequest);

        // 다시 매칭해서 응답 DTO 만들어주기
        List<BookItem> bookItemList = new ArrayList<>();
        for(CartDetail cartDetail : cartDetails) {
            for(BookApiResponse book : books) {
                if(cartDetail.getBookId() == book.bookId()) {
                    BookItem bookItem = new BookItem(book.bookId(), book.title(), book.price(), cartDetail.getQuantity());
                    bookItemList.add(bookItem);
                }
            }
        }

        return bookItemList;
    }
}
