package com.nhnacademy.order_payments.service.order;

import com.nhnacademy.order_payments.dto.order.OrderSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderValidationService {

    /**
     * Order 정보 검증하는 메서드
     * 길어질거 같으니까
     * 그냥 클래스로 분리하고 Bean 주입 받는게 좋을 듯
     */

    /** 검증해야할 목록
     *  1. 도서 재고
     *  2. 도서의 정가, 할인율, 할인가
     *  3. 회원의 잔여 포인트
     *  4. 쿠폰의 사용 가능 여부
     *  5. 배송 정책, 책정된 배송비
     *  6. 포장에 대한 가격
     *  7. 최종 결제 금액
     *  8. 최종 포인트 ??
     */
    public boolean validateOrder(Long userId, OrderSummaryDto dto) { // boolean 으로 반환하는게 과연 맞는지?





        return true;
    }

}
