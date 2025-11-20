package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.dto.BookDto;
import com.nhnacademy.order_payments.dto.BookInfo;
import com.nhnacademy.order_payments.infra.BookApiClient;
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
    private final HashOperations<String, Long, BookInfo> hashOps;
    private final BookApiClient bookApiClient;
    public GuestCartService(RedisTemplate<String, Object> redisTemplate, BookApiClient bookApiClient) {
        this.redisTemplate = redisTemplate;
        this.bookApiClient = bookApiClient;
        this.hashOps = redisTemplate.opsForHash();
    }

    // 요청된 수량만큼 redis에 추가
    public void addBook(String guestId, Long bookId, int quantity) {
        isNull(guestId, bookId);

        BookDto bookDto = bookApiClient.getBookInfo(bookId);
//        BookDto bookDto = new BookDto("TestBook", 20000); // 테스트 데이터

        BookInfo bookInfo = new BookInfo(bookId, bookDto.getTitle(), bookDto.getPrice(), quantity);

        hashOps.put(guestId, bookId, bookInfo);
        redisTemplate.expire(guestId, Duration.ofDays(1)); // TTL 설정
    }

    // 저장된 도서의 목록 반환
    public Map<Long, BookInfo> getBookList(String guestId) {
        isNull(guestId, 1000L); // bookId는 아무 값이나 넣은거

        Map<Object, Object> booksMap = redisTemplate.opsForHash().entries(guestId);
        Map<Long, BookInfo> resMap = new HashMap<>();

        for(Object obj : booksMap.values()) {
            BookInfo bookInfo = (BookInfo) obj;
            Long bookId = bookInfo.bookId();
            resMap.put(bookId, bookInfo);
        }

        return resMap;
    }

    // 저장된 특정 도서의 수량 반환
    public int getBookQuantity(String guestId, Long bookId) {
        isNull(guestId, bookId);

        return hashOps.get(guestId, bookId).quantity(); // null 체크 안해줘도 되나?
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
