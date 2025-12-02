//package com.nhnacademy.order_payments.service;
//
//
//import com.nhnacademy.order_payments.entity.CartDetail;
//import com.nhnacademy.order_payments.client.BookApiClient;
//import com.nhnacademy.order_payments.repository.CartDetailRepository;
//import com.nhnacademy.order_payments.repository.CartRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.mockito.Mockito.*;
//
//
//@ExtendWith(MockitoExtension.class)
//public class CartServiceTest {
//
//    @InjectMocks
//    private CartService cartService;
//
//    @Mock
//    private CartRepository cartRepository;
//
//    @Mock
//    private BookApiClient bookApiClient;
//
//    @Mock
//    private CartDetailRepository cartDetailRepository;
//
//    private static final Long userId = 12L;
//    private static final Long bookId = 234234L;
//    private static final int quantity = 1;
//
//    @Test
//    @DisplayName("장바구니 DB에 도서 추가 성공")
//    void addBookTest_Success() {
//
////        BookDto mockBook = new BookDto("testBook", 30000);
//
//        when(cartRepository.findById(userId)).thenReturn(Optional.empty());
//
////        when(bookApiClient.getBookInfo(bookId)).thenReturn(mockBook);
//
////        memberCartService.addBook(userId, bookId, 1);
//
//        verify(cartDetailRepository, times(1)).save(any(CartDetail.class));
//
//    }
//
//
//
//}
//
//
