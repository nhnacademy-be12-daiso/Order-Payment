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

package com.nhnacademy.order_payments.dto.order;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문서 작성 시
 * 사용자가 사용할 수 있는 쿠폰 정보
 */
// 쿠폰 기본 정보 응답
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponResponse {
    private Long userCouponId;
    private Long userId;
    private CouponPolicyResponse couponPolicy;  // 중첩 객체
    private String status;  // CouponStatus
    private LocalDateTime issuedAt;
    private LocalDateTime expiryAt;
    private LocalDateTime usedAt;

    private Long targetId; // 카테고리 or 특정 도서 판별하는 컬럼
    private String itemName; // 카테고리/도서 쿠폰 view를 위한 컬럼
}
