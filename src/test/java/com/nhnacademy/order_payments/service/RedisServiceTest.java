package com.nhnacademy.order_payments.service;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RedisServiceTest {

    @InjectMocks
    private RedisService redisService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Long, Integer> hashOps;

    private final String UUID = "uuid23234";
    private final Long bookId = 23423L;
    private final int quantity = 1;


    @BeforeEach
    void setUp() {
        doReturn(hashOps).when(redisTemplate).opsForHash();

        redisService = new RedisService(redisTemplate);
    }


    @Test
    @DisplayName("Redis에 도서 추가 성공")
    void addBookTest_Success() {

        redisService.addBook(UUID, bookId, quantity);

        verify(hashOps, times(1)).put(
                eq(UUID),
                eq(bookId),
                eq(quantity)
        );
    }

    @Test
    @DisplayName("Redis에 도서 추가 실패 - UUID null")
    void addBookTest_Fail() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            redisService.addBook(null, bookId, quantity);
        });

        verify(hashOps, never()).put(anyString(), anyLong(), anyInt());
    }


}
