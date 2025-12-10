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

package com.nhnacademy.order_payments.dto.response.order;

import com.nhnacademy.order_payments.model.OrderDetailStatus;

public record OrderDetailResponse(
        Long orderDetailId,
        Long bookId,
        String bookTitle,
        String bookCoverImage,
        Long price, //구매당시 금액
        Integer quantity,
        String packagingName, // 포장을 안했으면 null, 했으면 포장한 포장지 이름
        OrderDetailStatus orderDetailStatus,
        Long reviewId
) {
}
