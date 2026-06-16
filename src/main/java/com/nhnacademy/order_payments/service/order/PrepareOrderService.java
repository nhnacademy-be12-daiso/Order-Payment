/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2025. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.order_payments.service.order;

import com.nhnacademy.order_payments.client.BookApiClient;
import com.nhnacademy.order_payments.client.CouponApiClient;
import com.nhnacademy.order_payments.client.UserApiClient;
import com.nhnacademy.order_payments.dto.cart.BookApiRequest;
import com.nhnacademy.order_payments.dto.order.CouponResponse;
import com.nhnacademy.order_payments.dto.order.InternalBookInfoResponse;
import com.nhnacademy.order_payments.dto.order.InternalBooksInfoResponse;
import com.nhnacademy.order_payments.dto.order.PackagingDto;
import com.nhnacademy.order_payments.dto.order.PrepareOrderDto;
import com.nhnacademy.order_payments.dto.order.PrepareOrderRequest;
import com.nhnacademy.order_payments.dto.order.UserInfoResponse;
import com.nhnacademy.order_payments.dto.response.DeliveryPolicyResponse;
import com.nhnacademy.order_payments.entity.DeliveryPolicy;
import com.nhnacademy.order_payments.exception.ExternalServiceException;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.repository.DeliveryPolicyRepository;
import com.nhnacademy.order_payments.repository.PackagingRepository;
import feign.FeignException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * OrderService 개요
 * <p>
 * 각 API들에게서 필요한 정보들 수합해서 FrontServer로 보내주기
 * 1. 도서 : 도서 정가 + 할인 정보
 * 2. 쿠폰 : 사용 가능한 쿠폰
 * 3. 회원 : 사용가능한 포인트 + 현재 등급
 * 4. 배송비 : 배송비 정책
 * 5. 포장지 : 포장지 정책
 * 6. 더 있나 ....? ? ? ? ??
 * <p>
 * <p>
 * **** 참고 ****
 * Order API는 단순하게 필요한 정보를 넘겨주는 역할만 하고,
 * 실제 비즈니스 로직은 FrontServer에 구현하기로 하자  ---> 반박 가능
 */


/**
 * 추가적인 리팩토링 사항
 * 1. API 통신 비동기로 바꾸기
 * 2. 중복 코드 간소화
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PrepareOrderService {

    private final BookApiClient bookApiClient;
    private final UserApiClient userApiClient;
    private final CouponApiClient couponApiClient;

    private final PackagingRepository packagingRepository;
    private final DeliveryPolicyRepository deliveryPolicyRepository;

    /**
     * 회원용 주문서 작성 데이터 조회
     */
    // @Transactional
    // ----> 읽기 작업 뿐이기 때문에 트랜잭션 굳이 안해도 됨
    public PrepareOrderDto prepareOrderInfo(Long userId, List<PrepareOrderRequest> requestList) {
        // 5개 외부 호출을 병렬 실행 (기존 순차 호출 대비 응답 시간 최대 5× 단축)
        CompletableFuture<InternalBooksInfoResponse> booksFuture =
                CompletableFuture.supplyAsync(() -> getBooks(requestList));
        CompletableFuture<List<PackagingDto>> packagingFuture =
                CompletableFuture.supplyAsync(this::getPackagings);
        CompletableFuture<DeliveryPolicyResponse> deliveryFuture =
                CompletableFuture.supplyAsync(this::getDeliveryPolicy);
        CompletableFuture<UserInfoResponse> userFuture =
                CompletableFuture.supplyAsync(() -> userApiClient.getUserInfo(userId).getBody());
        CompletableFuture<List<CouponResponse>> couponFuture =
                CompletableFuture.supplyAsync(() -> couponApiClient.getAvailableCoupons(userId).getBody());

        try {
            CompletableFuture.allOf(booksFuture, packagingFuture, deliveryFuture, userFuture, couponFuture).join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FeignException) {
                log.error("[PrepareOrderService] 회원 - 외부 API 통신 간 오류 발생: {}", cause.getMessage());
                throw new ExternalServiceException("[PrepareOrderService] 회원 - 외부 API 통신 간 오류 발생");
            }
            if (cause instanceof NotFoundOrderException nfe) {
                throw nfe;
            }
            throw new ExternalServiceException("[PrepareOrderService] 회원 - 예상치 못한 오류: " + cause.getMessage());
        }

        InternalBooksInfoResponse booksInfoResponse = booksFuture.join();
        List<PackagingDto> packagingList = packagingFuture.join();
        DeliveryPolicyResponse deliveryPolicyResponse = deliveryFuture.join();
        UserInfoResponse userInfoResponse = userFuture.join();
        List<CouponResponse> couponResponseList = couponFuture.join();

        if (booksInfoResponse == null || deliveryPolicyResponse == null ||
                userInfoResponse == null || couponResponseList == null) {
            throw new NotFoundOrderException("[PrepareOrderService] 회원 - 외부 API에서 null값 넘어옴");
        }

        return new PrepareOrderDto(
                booksInfoResponse,
                userInfoResponse,
                couponResponseList,
                packagingList,
                deliveryPolicyResponse
        );
    }


    // 비회원 주문시 필요한 최소 정보
    public PrepareOrderDto prepareGuestOrderInfo(List<PrepareOrderRequest> requestList) {
        // 3개 호출 병렬 실행
        CompletableFuture<InternalBooksInfoResponse> booksFuture =
                CompletableFuture.supplyAsync(() -> getBooks(requestList));
        CompletableFuture<List<PackagingDto>> packagingFuture =
                CompletableFuture.supplyAsync(this::getPackagings);
        CompletableFuture<DeliveryPolicyResponse> deliveryFuture =
                CompletableFuture.supplyAsync(this::getDeliveryPolicy);

        try {
            CompletableFuture.allOf(booksFuture, packagingFuture, deliveryFuture).join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FeignException) {
                log.error("[PrepareOrderService] 비회원 - 외부 API 통신 간 오류 발생: {}", cause.getMessage());
                throw new ExternalServiceException("[PrepareOrderService] 비회원 - 외부 API 통신 간 오류 발생");
            }
            if (cause instanceof NotFoundOrderException nfe) {
                throw nfe;
            }
            throw new ExternalServiceException("[PrepareOrderService] 비회원 - 예상치 못한 오류: " + cause.getMessage());
        }

        InternalBooksInfoResponse booksInfoResponse = booksFuture.join();
        List<PackagingDto> packagingList = packagingFuture.join();
        DeliveryPolicyResponse deliveryPolicyResponse = deliveryFuture.join();

        if (booksInfoResponse == null || deliveryPolicyResponse == null) {
            throw new NotFoundOrderException("[PrepareOrderService] 비회원 - 외부 API에서 null값 넘어옴");
        }

        return new PrepareOrderDto(
                booksInfoResponse,
                packagingList,
                deliveryPolicyResponse
        );
    }

    // 도서 정보
    private InternalBooksInfoResponse getBooks(List<PrepareOrderRequest> requestList) {
        InternalBooksInfoResponse booksInfoResponse;

        // requestList에서 bookId만 추출해서 리스트 생성
        List<Long> bookIdList = requestList.stream()
                .map(PrepareOrderRequest::bookId)
                .toList();

        // 도서 정보
        booksInfoResponse = bookApiClient.getBookInfos(new BookApiRequest(bookIdList));

            // 받아온 도서 정보에 수량과 합계 주입
            List<InternalBookInfoResponse> updateBookInfos = booksInfoResponse.orderBookInfoRespDTOList().stream()
                    .map(book -> new InternalBookInfoResponse(
                            book.bookId(),
                            book.title(),
                            book.price(),
                            book.stock(),
                            book.staus(),
                            book.discountPercentage(),
                            book.discountPrice(),
                            book.coverImage(),
                            book.volumeNo(),
                            book.isPackaging()
                    )).toList();

        // 값을 채운 리스트로 다시 덮어씌움
        return new InternalBooksInfoResponse(updateBookInfos);
    }

    // 포장지 정보: 현재 존재하는 포장지 모두 불러옴
    private List<PackagingDto> getPackagings() {
        return packagingRepository.findAllByEnabled(true).stream()
                .map(PackagingDto::new)
                .toList();
    }

    // 배송비 정책 정보
    private DeliveryPolicyResponse getDeliveryPolicy() {
        DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findTopByOrderByDeliveryPolicyIdDesc()
                .orElseThrow(() -> new NotFoundOrderException("배송 정책을 찾을 수 없습니다."));

        return new DeliveryPolicyResponse(deliveryPolicy);
    }

}
