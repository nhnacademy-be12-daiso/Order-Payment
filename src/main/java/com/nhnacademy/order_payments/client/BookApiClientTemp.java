package com.nhnacademy.order_payments.client;

import com.nhnacademy.order_payments.dto.cart.BookApiRequest;
import com.nhnacademy.order_payments.dto.order.BookInfo;
import com.nhnacademy.order_payments.dto.order.BookInfoResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Book API 완성이 안되어서 그냥 대충 임시로 만든 클래스
 */

@Component
public class BookApiClientTemp {

    public BookInfoResponse getBookInfos(BookApiRequest bookApiRequest) {


        int min = 10000;
        int max = 30000;

        // 2. 100단위로 변환된 정수 범위 (100 ~ 300)
        int minHundreds = min / 100; // 100
        int maxHundreds = max / 100; // 300

        // 3. 생성할 수 있는 총 개수 (300 - 100 + 1 = 201개)
        int range = maxHundreds - minHundreds + 1; // 201
        List<BookInfo> bookInfos = new ArrayList<>();

        for(Long bookId : bookApiRequest.bookIdList()) {
            int randomOffset = (int) (Math.random() * range);
            int finalRandomValue = (randomOffset + minHundreds) * 100;
            bookInfos.add(new BookInfo(bookId, "title:"+ bookId.toString(), finalRandomValue));
        }

        return new BookInfoResponse(bookInfos);
    }

}
