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

/**
 * 주문서 작성 시,
 * Book API에서 책에 대한 정보를 담아옴
 * 받아올 DTO임
 */
public record BookInfo(Long bookId,
                       String title,
                       Long price,
                       Integer quantity,
                       Long totalPrice // price * quantity
                       // Integer stock
                       // 재고는 결제할때 '직접' 호출하는게 나을 듯
                       // --> 결제 당시의 수량을 정확히 알아야하므로

                       // >>>>>> 더 받아올 정보가 뭐가 있을지 고민해보기 <<<<<<<<
) {
}
