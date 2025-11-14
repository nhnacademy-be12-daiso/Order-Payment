package com.nhnacademy.order_payments.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class GuestCartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, String, String> hashOps;
    public GuestCartService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    // 요청된 수량만큼 redis에 추가
    public void addBook(String guestId, Long bookId, int quantity) {
        isNull(guestId, bookId);
        hashOps.put(guestId, bookId.toString(), String.valueOf(quantity));
        redisTemplate.expire(guestId, Duration.ofDays(1)); // TTL 설정
    }

    // 저장된 도서의 목록 반환
    public Map<Long, Integer> getBookList(String guestId) {
        isNull(guestId, 1000L); // bookId는 아무 값이나 넣은거

        Map<Object, Object> booksMap = redisTemplate.opsForHash().entries(guestId);
        Map<Long, Integer> resMap = new HashMap<>();

        for(Map.Entry<Object, Object> entry : booksMap.entrySet()) {
            String strBookId = (String) entry.getKey();
            Long bookId = Long.parseLong(strBookId);
            Integer quantity = Integer.parseInt(entry.getValue().toString());
            resMap.put(bookId, quantity);
        }

        return resMap;
    }

    // 저장된 특정 도서의 수량 반환
    public int getBookQuantity(String guestId, Long bookId) {
        isNull(guestId, bookId);

        Integer quantity = Integer.valueOf(hashOps.get(guestId, bookId));

        return quantity != null ? quantity : 0;
    }

    // 특정 도서를 장바구니에서 삭제
    public void deleteBook(String guestId, Long bookId) {
        isNull(guestId, bookId);

        hashOps.delete(guestId, String.valueOf(bookId));
        log.info("삭제 성공 : {}", bookId);
    }

    // 유효성 검증 메서드
    public void isNull(String guestId, Long bookId) {
        if(!StringUtils.hasText(guestId)) {
            throw new IllegalArgumentException("유효하지 않은 UUID입니다 : " + guestId);
        }
        else if(bookId == null) {
            throw new IllegalArgumentException("유효하지 않은 Book ID입니다 : " + bookId);
        }
    }

}
