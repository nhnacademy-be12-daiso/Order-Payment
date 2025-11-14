package com.nhnacademy.order_payments.service;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class GuestCartServiceTest {

    @InjectMocks
    private GuestCartService guestCartService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Long, Integer> hashOps;

    private static final String UUID = "uuid23234";
    private static final Long bookId = 23423L;
    private static final int quantity = 1;

    static Stream<Arguments> invalidInputProvider() {
        return Stream.of(
                Arguments.of(null, bookId, quantity),
                Arguments.of("", bookId, quantity),
                Arguments.of(" ", bookId, quantity),
                Arguments.of(UUID, null, quantity)
        );
    }



    @BeforeEach
    void setUp() {
        doReturn(hashOps).when(redisTemplate).opsForHash();

        guestCartService = new GuestCartService(redisTemplate);
    }


    @Test
    @DisplayName("Redis에 도서 추가 성공")
    void addBookTest_Success() {

        guestCartService.addBook(UUID, bookId, quantity);
        verify(hashOps, times(1)).put(
                eq(UUID),
                eq(bookId),
                eq(quantity)
        );
        verify(redisTemplate, times(1)).expire(eq(UUID), any(Duration.class));
    }

    @ParameterizedTest
    @MethodSource("invalidInputProvider")
    @DisplayName("Redis에 도서 추가 실패 - 유효하지 않은 입력")
    void addBookTest_Fail(String UUID, Long bookId, Integer quantity) {

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            guestCartService.addBook(UUID, bookId, quantity);
        });

        verify(hashOps, never()).put(anyString(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("Redis에서 도서 삭제")
    void deleteTest_Success() {

        guestCartService.deleteBook(UUID, bookId);

        verify(hashOps, times(1)).delete(UUID, bookId);
    }


}
