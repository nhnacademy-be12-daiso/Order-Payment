package com.nhnacademy.order_payments.service.cart;

import com.nhnacademy.order_payments.client.BookApiClient;
import com.nhnacademy.order_payments.dto.cart.BookApiRequest;
import com.nhnacademy.order_payments.dto.cart.BookApiResponse;
import com.nhnacademy.order_payments.dto.cart.BookItem;
import com.nhnacademy.order_payments.entity.Cart;
import com.nhnacademy.order_payments.entity.CartDetail;
import com.nhnacademy.order_payments.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServiceTest {

    @Mock
    BookApiClient bookApiClient;

    @Mock
    CartRepository cartRepository;

    CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(bookApiClient, cartRepository);
    }

    @Test
    @DisplayName("Book API 응답을 Map으로 변환해 O(1) 조회 - 결과가 cartDetail 순서/수량과 일치한다")
    void getCartList_mapsBookResponseCorrectly() {
        Cart cart = mockCartWith(List.of(
                cartDetail(1L, 2),
                cartDetail(2L, 1)
        ));
        when(cartRepository.findCartWithDetailsByUserId(10L)).thenReturn(Optional.of(cart));
        when(bookApiClient.getBookList(any(BookApiRequest.class))).thenReturn(List.of(
                new BookApiResponse(1L, "자바의 정석", 30_000L),
                new BookApiResponse(2L, "스프링 부트", 25_000L)
        ));

        List<BookItem> result = cartService.getCartList(10L);

        assertEquals(2, result.size());
        BookItem first = result.stream().filter(b -> b.bookId() == 1L).findFirst().orElseThrow();
        assertEquals("자바의 정석", first.title());
        assertEquals(2, first.quantity());
    }

    @Test
    @DisplayName("Book API에 없는 bookId는 결과에 포함되지 않는다")
    void getCartList_skipsUnknownBookId() {
        Cart cart = mockCartWith(List.of(
                cartDetail(99L, 1) // Book API에 없는 ID
        ));
        when(cartRepository.findCartWithDetailsByUserId(10L)).thenReturn(Optional.of(cart));
        when(bookApiClient.getBookList(any())).thenReturn(List.of(
                new BookApiResponse(1L, "다른 책", 10_000L)
        ));

        List<BookItem> result = cartService.getCartList(10L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("장바구니가 없으면 새로 생성 후 빈 목록을 반환한다")
    void getCartList_createsNewCartWhenAbsent() {
        Cart newCart = mock(Cart.class);
        when(newCart.getDetails()).thenReturn(List.of());
        when(cartRepository.findCartWithDetailsByUserId(20L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);
        when(bookApiClient.getBookList(any())).thenReturn(List.of());

        List<BookItem> result = cartService.getCartList(20L);

        assertTrue(result.isEmpty());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("bookId가 200 이상인 Long 값도 == 비교 오류 없이 올바르게 매칭된다")
    void getCartList_largeBookIdMatchesCorrectly() {
        long largeId = 300L; // 캐시 범위(127) 초과 → 기존 == 비교는 false
        Cart cart = mockCartWith(List.of(cartDetail(largeId, 3)));
        when(cartRepository.findCartWithDetailsByUserId(5L)).thenReturn(Optional.of(cart));
        when(bookApiClient.getBookList(any())).thenReturn(List.of(
                new BookApiResponse(largeId, "큰 ID 책", 15_000L)
        ));

        List<BookItem> result = cartService.getCartList(5L);

        assertEquals(1, result.size());
        assertEquals(largeId, result.get(0).bookId());
    }

    @Test
    @DisplayName("Book API는 단 1번만 호출된다 (배치 조회)")
    void getCartList_callsBookApiOnlyOnce() {
        Cart cart = mockCartWith(List.of(cartDetail(1L, 1), cartDetail(2L, 2), cartDetail(3L, 1)));
        when(cartRepository.findCartWithDetailsByUserId(7L)).thenReturn(Optional.of(cart));
        when(bookApiClient.getBookList(any())).thenReturn(List.of());

        cartService.getCartList(7L);

        verify(bookApiClient, times(1)).getBookList(any());
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private Cart mockCartWith(List<CartDetail> details) {
        Cart cart = mock(Cart.class);
        when(cart.getDetails()).thenReturn(details);
        return cart;
    }

    private CartDetail cartDetail(long bookId, int quantity) {
        CartDetail cd = mock(CartDetail.class);
        when(cd.getBookId()).thenReturn(bookId);
        when(cd.getQuantity()).thenReturn(quantity);
        return cd;
    }
}
