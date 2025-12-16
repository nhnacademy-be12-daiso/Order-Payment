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

import java.util.List;

/**
 * [결제하기] 버튼을 눌렀을때 넘어오는 '확정된' 주문 정보 DTO
 * 이거 토대로 주문 로직 수행하면 됨
 */
public record OrderSummaryDto(
        List<BookSummaryDto> bookList, // 주문 상품
        OrdererSummaryDto ordererSummaryDto, // 주문자 정보
        DeliverySummaryDto deliverySummaryDto, // 배송 정보
        Long usedPoint, // 포인트 사용
        Long savedPoint, // 포인트 적립
        Long totalPrice // 최종 금액
) {
}
