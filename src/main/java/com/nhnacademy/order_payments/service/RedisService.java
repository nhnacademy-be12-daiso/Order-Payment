package com.nhnacademy.order_payments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, Long, Integer> hashOps;
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    // 요청된 수량만큼 redis에 추가
    public void addBook(String guestId, Long bookId, int quantity) {
        isNull(guestId, bookId);
        hashOps.put(guestId, bookId, quantity);
    }

    // 저장된 도서의 목록 반환
    public Map<String, Integer> getBookList(String guestId) {
        isNull(guestId, 1000L); // bookId는 아무 값이나 넣은거

        Map<Object, Object> booksMap = redisTemplate.opsForHash().entries(guestId);
        Map<String, Integer> resMap = new HashMap<>();

        for(Map.Entry<Object, Object> entry : booksMap.entrySet()) {
            String bookId = (String) entry.getKey();
            Integer quantity = (Integer) entry.getValue();
            resMap.put(bookId, quantity);
        }

        return resMap;
    }

    // 저장된 특정 도서의 수량 반환
    public int getBookQuantity(String guestId, Long bookId) {
        isNull(guestId, bookId);

        Integer quantity = hashOps.get(guestId, bookId);

        return quantity != null ? quantity : 0;
    }

    // 특정 도서를 장바구니에서 삭제
    public void deleteBook(String guestId, Long bookId) {
        isNull(guestId, bookId);

        hashOps.delete(guestId, bookId);
    }

    // 유효성 검증 메서드
    public void isNull(String guestId, Long bookId) {
        if(!StringUtils.hasText(guestId) || bookId == null) {
            throw new IllegalArgumentException("유효하지 않은 ID입니다."); // ----> 예외처리 이후에 커스텀으로
        }
    }

}
