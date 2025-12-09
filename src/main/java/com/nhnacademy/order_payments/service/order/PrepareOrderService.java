package com.nhnacademy.order_payments.service.order;

import com.nhnacademy.order_payments.client.BookApiClient;
import com.nhnacademy.order_payments.client.BookApiClientTemp;
import com.nhnacademy.order_payments.client.CouponApiClient;
import com.nhnacademy.order_payments.client.UserApiClient;
import com.nhnacademy.order_payments.dto.cart.BookApiRequest;
import com.nhnacademy.order_payments.dto.order.*;
import com.nhnacademy.order_payments.entity.DeliveryPolicy;
import com.nhnacademy.order_payments.entity.Packaging;
import com.nhnacademy.order_payments.exception.ExternalServiceException;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.repository.DeliveryPolicyRepository;
import com.nhnacademy.order_payments.repository.PackagingRepository;
import com.nhnacademy.order_payments.service.PackagingService;
import feign.FeignException;
import jakarta.transaction.Transactional;
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


/** 추가적인 리팩토링 사항
 * 1. API 통신 비동기로 바꾸기
 * 2. 중복 코드 간소화
 */

@Service
@RequiredArgsConstructor
public class PrepareOrderService {

    private final BookApiClient bookApiClient;
    private final UserApiClient userApiClient;
    private final CouponApiClient couponApiClient;
    private final PackagingRepository packagingRepository;
    private final DeliveryPolicyRepository deliveryPolicyRepository;
    private final BookApiClientTemp bookApiClientTemp;

    // 주문서 작성을 위한 데이터를 넘겨주는 메서드
//    @Transactional
    // ----> 읽기 작업 뿐이기 때문에 트랜잭션 굳이 안해도 됨
    public PrepareOrderDto prepareOrderInfo(Long userId, List<Long> bookIdList) {

        BookInfoResponse bookInfoResponse = null;
        UserInfoResponse userInfoResponse = null;
        List<CouponResponse> couponResponseList = null;
        List<PackagingDto> packagingList = null;
        DeliveryPolicyDto deliveryDto = null;

        try {
            // 도서 정보
//            bookInfoResponse = bookApiClient.getBookInfos(new BookApiRequest(bookIdList));
            bookInfoResponse = bookApiClientTemp.getBookInfos(new BookApiRequest(bookIdList));
            // ----> 임시로

            // 회원 정보
            userInfoResponse = userApiClient.getUserInfo(userId).getBody();

            // 쿠폰 정보
            couponResponseList = couponApiClient.getAvailableCoupons(userId).getBody();

            // 지금 있는 포장지 다 긁어옴
            List<Packaging> rawPackagings = packagingRepository.findAll();
            packagingList = rawPackagings.stream()
                    .map(PackagingDto::new)
                    .toList();

            // 배송 정책
            DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findByDeliveryPolicyName("DEFAULT");
            // --> 일단 기본 3000원 가져옴
            deliveryDto = new DeliveryPolicyDto(deliveryPolicy);
            // 여기 있는게 맞는지는 모르겠음
        } catch(FeignException e) {
            throw new ExternalServiceException("외부 API 통신 간 오류 발생");
        }

        // null이 반환된 경우 ---> 이럴 경우가 있나?
        if(bookInfoResponse == null || userInfoResponse == null ||
                couponResponseList == null || deliveryDto == null
        ) {
            throw new NotFoundOrderException("외부 API에서 null값 넘어옴");
        }
        // -----> 포장 정책은 진짜 비어있을 수 있어서 null체크 안함


        // 모든 데이터 수합한 dto
        PrepareOrderDto dto = new PrepareOrderDto(bookInfoResponse,
                userInfoResponse,
                couponResponseList,
                packagingList,
                deliveryDto
        );

        return dto;
    }


    // 비회원 주문시 필요한 최소 정보
    public PrepareOrderDto prepareGuestOrderInfo(List<Long> bookIdList) {

        BookInfoResponse bookInfoResponse = null;
        List<PackagingDto> packagingList = null;
        DeliveryPolicyDto deliveryDto = null;

        try {
            // 도서 정보
//            bookInfoResponse = bookApiClient.getBookInfos(new BookApiRequest(bookIdList));
            bookInfoResponse = bookApiClientTemp.getBookInfos(new BookApiRequest(bookIdList));
            // ----> 임시로


            // 지금 있는 포장지 다 긁어옴
            List<Packaging> rawPackagings = packagingRepository.findAll();
            packagingList = rawPackagings.stream()
                    .map(PackagingDto::new)
                    .toList();

            // 배송 정책
            DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findByDeliveryPolicyName("DEFAULT");
            // --> 일단 기본 3000원 가져옴
            deliveryDto = new DeliveryPolicyDto(deliveryPolicy);
            // 여기 있는게 맞는지는 모르겠음

        } catch(FeignException e) {
            throw new ExternalServiceException("외부 API 통신 간 오류 발생");
        }

        // null이 반환된 경우 ---> 이럴 경우가 있나?
        if(bookInfoResponse == null || deliveryDto == null) {
            throw new NotFoundOrderException("외부 API에서 null값 넘어옴");
        }


        PrepareOrderDto dto = new PrepareOrderDto(
                bookInfoResponse,
                packagingList,
                deliveryDto
        );

        return dto;
    }



}
