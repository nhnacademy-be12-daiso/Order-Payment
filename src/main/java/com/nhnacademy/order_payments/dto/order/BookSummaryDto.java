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

import java.math.BigDecimal;

/**
 * 책 별로 적용되는 애들을 묶음
 * 1. 책
 * 2. 책 자체의 정가 + 할인가 + 할인률
 * 3. 적용된 쿠폰
 */

public record BookSummaryDto(
        Long bookId, // 도서 PK
        Long couponId, // 적용한 쿠폰
        Long price, // 책 정가
        BigDecimal discountPercentage, // 할인율
        Long discountPrice, // 할인 금액
        Integer quantity, // 수량
        Long packagingId // 선택한 포장지
) {
}
