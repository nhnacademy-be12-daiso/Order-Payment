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
 * 주문서 작성 시,
 * Book API에서 책에 대한 정보를 담아옴
 * 받아올 DTO임
 */
public record BookInfo(
        Long bookId,
        String title,
        Long price,
        Integer quantity,
        BigDecimal discountPercentage,
        Long discountPrice,
        Long totalPrice
) {
}
