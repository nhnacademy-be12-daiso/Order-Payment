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
import java.util.List;

/**
 * 주문서 작성 시
 * user에게서 받아올 정보
 * 1. 사용가능한 포인트
 * 2. 현재 회원의 등급
 */

public record UserInfoResponse(Long userCreatedId,
                               String userName,
                               String phoneNumber,
                               String email,
                               String gradeName,
                               BigDecimal pointRate,
                               BigDecimal point,
                               List<InternalAddressResponse> addresses
) {
}
