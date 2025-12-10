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

package com.nhnacademy.order_payments.client;

import com.nhnacademy.order_payments.dto.cart.BookApiRequest;
import com.nhnacademy.order_payments.dto.order.BookInfo;
import com.nhnacademy.order_payments.dto.order.BookInfoResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Book API 완성이 안되어서 그냥 대충 임시로 만든 클래스
 */

@Component
public class BookApiClientTemp {

    public BookInfoResponse getBookInfos(BookApiRequest bookApiRequest) {


        int min = 10000;
        int max = 30000;

        // 2. 100단위로 변환된 정수 범위 (100 ~ 300)
        int minHundreds = min / 100; // 100
        int maxHundreds = max / 100; // 300

        // 3. 생성할 수 있는 총 개수 (300 - 100 + 1 = 201개)
        int range = maxHundreds - minHundreds + 1; // 201
        List<BookInfo> bookInfos = new ArrayList<>();

        for (Long bookId : bookApiRequest.bookIdList()) {
            int randomOffset = (int) (Math.random() * range);
            Long finalRandomValue = (randomOffset + minHundreds) * 100L;
            bookInfos.add(new BookInfo(bookId, "title:" + bookId.toString(), finalRandomValue, null, null));
        }

        return new BookInfoResponse(bookInfos);
    }

}
