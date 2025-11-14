package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.service.GuestCartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class GuestCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuestCartService guestCartService;

    private final String guestId = "UUID_23423";
    private final Long bookId = 2342L; // 임의의 숫자
    private final int quantity = 2;

    @Test
    @DisplayName("장바구니에 도서 추가 요청")
    void addCartItem_Success() throws Exception {

        doNothing().when(guestCartService).addBook(anyString(), anyLong(), anyInt());

        mockMvc.perform(post("/guest/carts")
                .header("X-Guest-ID", guestId)
                .param("bookId", (bookId).toString())
                .param("quantity", String.valueOf(quantity))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk());

        verify(guestCartService, times(1)).addBook(eq(guestId), eq(bookId), eq(quantity));
    }

    @Test
    @DisplayName("장바구니 도서 목록 요청")
    void getCartItemList_Success() throws Exception {

    }

    @Test
    @DisplayName("장바구니의 특정 도서 삭제")
    void deleteCartItem_Success() throws Exception {
        doNothing().when(guestCartService).deleteBook(anyString(), anyLong());

        mockMvc.perform(delete("/guest/carts")
                .header("X-Guest-ID", guestId)
                .param("bookId", bookId.toString()))
            .andExpect(status().isOk());

        verify(guestCartService, times(1)).deleteBook(eq(guestId), eq(bookId));
    }


    @Test
    @DisplayName("X-Guest-ID 헤더 누락시 400 응답")
    void notFoundHeader_400() throws Exception {

        mockMvc.perform(post("/guest/carts"))
                .andExpect(status().isBadRequest());

        verify(guestCartService, never()).addBook(anyString(), anyLong(), anyInt());
    }

}
