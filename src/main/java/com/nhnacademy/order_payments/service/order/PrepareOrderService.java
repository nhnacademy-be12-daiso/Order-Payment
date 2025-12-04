package com.nhnacademy.order_payments.service.order;

import com.nhnacademy.order_payments.client.BookApiClient;
import com.nhnacademy.order_payments.client.CouponApiClient;
import com.nhnacademy.order_payments.client.UserApiClient;
import com.nhnacademy.order_payments.dto.cart.BookApiRequest;
import com.nhnacademy.order_payments.dto.order.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** OrderService 개요
 *
 *  각 API들에게서 필요한 정보들 수합해서 FrontServer로 보내주기
 *  1. 도서 : 도서 정가 + 할인 정보
 *  2. 쿠폰 : 사용 가능한 쿠폰
 *  3. 회원 : 사용가능한 포인트 + 현재 등급
 *  4. 배송비 : 배송비 정책
 *  5. 포장지 : 포장지 정책
 *  6. 더 있나 ....? ? ? ? ??
 *
 *
 *  **** 참고 ****
 *  Order API는 단순하게 필요한 정보를 넘겨주는 역할만 하고,
 *  실제 비즈니스 로직은 FrontServer에 구현하기로 하자  ---> 반박 가능
 */

@Service
@RequiredArgsConstructor
public class PrepareOrderService {

    private final BookApiClient bookApiClient;
    private final UserApiClient userApiClient;
    private final CouponApiClient couponApiClient;

    // 주문서 작성을 위한 데이터를 넘겨주는 메서드
    public  PrepareOrderDto prepareOrderData(Long userId, List<Long> bookIdList) {

        // 도서 정보
        BookInfoResponse bookInfoResponse = bookApiClient.getBookInfos(new BookApiRequest(bookIdList));

        // 회원 정보
        UserInfoResponse userInfoResponse = userApiClient.getUserInfo(userId);

        // 쿠폰 정보
        CouponResponse couponResponse = couponApiClient.getAvailableCoupons(userId);

        // >>>>>> 배송비, 포장지 정책 추가해야함 <<<<<<<<

        // 모든 데이터 수합한 dto
        PrepareOrderDto dto = new PrepareOrderDto(bookInfoResponse, userInfoResponse, couponResponse); // 추가될지도


        return dto;
    }

}
