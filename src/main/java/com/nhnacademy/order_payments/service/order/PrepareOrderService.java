package com.nhnacademy.order_payments.service.order;

import com.nhnacademy.order_payments.client.BookApiClient;
import com.nhnacademy.order_payments.client.BookApiClientTemp;
import com.nhnacademy.order_payments.client.CouponApiClient;
import com.nhnacademy.order_payments.client.UserApiClient;
import com.nhnacademy.order_payments.dto.cart.BookApiRequest;
import com.nhnacademy.order_payments.dto.order.BookInfo;
import com.nhnacademy.order_payments.dto.order.BookInfoResponse;
import com.nhnacademy.order_payments.dto.order.CouponResponse;
import com.nhnacademy.order_payments.dto.order.DeliveryPolicyDto;
import com.nhnacademy.order_payments.dto.order.PackagingDto;
import com.nhnacademy.order_payments.dto.order.PrepareOrderDto;
import com.nhnacademy.order_payments.dto.order.UserInfoResponse;
import com.nhnacademy.order_payments.dto.request.order.PrepareOrderRequest;
import com.nhnacademy.order_payments.entity.DeliveryPolicy;
import com.nhnacademy.order_payments.entity.Packaging;
import com.nhnacademy.order_payments.exception.ExternalServiceException;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.repository.DeliveryPolicyRepository;
import com.nhnacademy.order_payments.repository.PackagingRepository;
import feign.FeignException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final BookApiClientTemp bookApiClientTemp;

    /**
     * 회원용 주문서 작성 데이터 조회
     */
    // @Transactional
    // ----> 읽기 작업 뿐이기 때문에 트랜잭션 굳이 안해도 됨
    public PrepareOrderDto prepareOrderInfo(Long userId, List<PrepareOrderRequest> requestList) {

        BookInfoResponse bookInfoResponse = null;
        UserInfoResponse userInfoResponse = null;
        List<CouponResponse> couponResponseList = null;
        List<PackagingDto> packagingList = null;
        DeliveryPolicyDto deliveryDto = null;

        try {
            // requestList에서 bookId만 추출해서 리스트 생성
            List<Long> bookIdList = requestList.stream()
                    .map(PrepareOrderRequest::bookId)
                    .toList();

            // requestList를 Map으로 변환
            Map<Long, Integer> quantityMap = requestList.stream()
                    .collect(Collectors.toMap(
                            PrepareOrderRequest::bookId,
                            PrepareOrderRequest::quantity
                    ));

            // 도서 정보
            // bookInfoResponse = bookApiClient.getBookInfos(new BookApiRequest(bookIdList));
            bookInfoResponse = bookApiClientTemp.getBookInfos(new BookApiRequest(bookIdList));
            // ----> 임시로

            // 받아온 도서 정보에 수량과 합계 주입
            List<BookInfo> updateBookInfos = bookInfoResponse.bookInfos().stream()
                    .map(book -> {
                        Integer quantity = quantityMap.getOrDefault(book.bookId(), 1);
                        // 총 가격 = 할인가 * 수량
                        Long finalPrice = book.discountPrice() * quantity;

                        return new BookInfo(
                                book.bookId(),
                                book.title(),
                                book.price(),
                                quantity,
                                book.discountPercentage(),
                                book.discountPrice(),
                                finalPrice
                        );
                    }).toList();

            // 값을 채운 리스트로 다시 덮어씌움
            bookInfoResponse = new BookInfoResponse(updateBookInfos);

            // 회원 정보
            userInfoResponse = userApiClient.getUserInfo(userId).getBody();

            // 쿠폰 정보
            couponResponseList = couponApiClient.getAvailableCoupons(userId).getBody();

            // 포장지 정보: 현재 존재하는 포장지 모두 불러옴
            List<Packaging> rawPackagings = packagingRepository.findAllByEnabled(true);
            packagingList = rawPackagings.stream()
                    .map(PackagingDto::new)
                    .toList();

            // 배송 정책
            DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findTopByOrderByDeliveryPolicyIdDesc()
                    .orElseThrow(() -> new NotFoundOrderException("배송 정책을 찾을 수 없습니다."));
            // --> 일단 기본 3000원 가져옴
            deliveryDto = new DeliveryPolicyDto(deliveryPolicy);
            // 여기 있는게 맞는지는 모르겠음

        } catch (FeignException e) {
            log.error("외부 API 통신 간 오류 발생: {}", e.getMessage());
            throw new ExternalServiceException("외부 API 통신 간 오류 발생");
        }

        // null이 반환된 경우 ---> 이럴 경우가 있나?
        if (bookInfoResponse == null || userInfoResponse == null || couponResponseList == null || deliveryDto == null) {
            throw new NotFoundOrderException("외부 API에서 null값 넘어옴");
        }
        // -----> 포장 정책은 진짜 비어있을 수 있어서 null체크 안함

        // 모든 데이터 수합한 dto
        return new PrepareOrderDto(bookInfoResponse,
                userInfoResponse,
                couponResponseList,
                packagingList,
                deliveryDto
        );
    }


    // 비회원 주문시 필요한 최소 정보
    public PrepareOrderDto prepareGuestOrderInfo(List<PrepareOrderRequest> requestList) {

        BookInfoResponse bookInfoResponse = null;
        List<PackagingDto> packagingList = null;
        DeliveryPolicyDto deliveryDto = null;

        try {
            // requestList에서 bookId만 추출해서 리스트 생성
            List<Long> bookIdList = requestList.stream()
                    .map(PrepareOrderRequest::bookId)
                    .toList();

            // requestList를 Map으로 변환
            Map<Long, Integer> quantityMap = requestList.stream()
                    .collect(Collectors.toMap(
                            PrepareOrderRequest::bookId,
                            PrepareOrderRequest::quantity
                    ));

            // 도서 정보
            // bookInfoResponse = bookApiClient.getBookInfos(new BookApiRequest(bookIdList));
            bookInfoResponse = bookApiClientTemp.getBookInfos(new BookApiRequest(bookIdList));
            // ----> 임시로

            // 받아온 도서 정보에 수량과 합계 주입
            List<BookInfo> updateBookInfos = bookInfoResponse.bookInfos().stream()
                    .map(book -> {
                        Integer quantity = quantityMap.getOrDefault(book.bookId(), 1);
                        // 총 가격 = 할인가 * 수량
                        Long finalPrice = book.discountPrice() * quantity;

                        return new BookInfo(
                                book.bookId(),
                                book.title(),
                                book.price(),
                                quantity,
                                book.discountPercentage(),
                                book.discountPrice(),
                                finalPrice
                        );
                    }).toList();

            // 값을 채운 리스트로 다시 덮어씌움
            bookInfoResponse = new BookInfoResponse(updateBookInfos);

            // 포장지 정보: 현재 존재하는 포장지 모두 불러옴
            List<Packaging> rawPackagings = packagingRepository.findAllByEnabled(true);
            packagingList = rawPackagings.stream()
                    .map(PackagingDto::new)
                    .toList();

            // 배송 정책
            DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findTopByOrderByDeliveryPolicyIdDesc()
                    .orElseThrow(() -> new NotFoundOrderException("배송 정책을 찾을 수 없습니다."));
            // --> 일단 기본 3000원 가져옴
            deliveryDto = new DeliveryPolicyDto(deliveryPolicy);
            // 여기 있는게 맞는지는 모르겠음

        } catch (FeignException e) {
            log.error("외부 API 통신 간 오류 발생: {}", e.getMessage());
            throw new ExternalServiceException("외부 API 통신 간 오류 발생");
        }

        // null이 반환된 경우 ---> 이럴 경우가 있나?
        if (bookInfoResponse == null || deliveryDto == null) {
            throw new NotFoundOrderException("외부 API에서 null값 넘어옴");
        }

        return new PrepareOrderDto(
                bookInfoResponse,
                packagingList,
                deliveryDto
        );
    }

}
