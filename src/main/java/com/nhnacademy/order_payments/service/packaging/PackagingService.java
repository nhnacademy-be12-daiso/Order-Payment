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

package com.nhnacademy.order_payments.service.packaging;

import com.nhnacademy.order_payments.dto.packaging.request.PackagingRequest;
import com.nhnacademy.order_payments.dto.packaging.response.PackagingResponse;

import java.util.List;

public interface PackagingService {

    // 포장 정책 이름 조회
    String getPackagingName(Long packagingId);

    // 포장 정책 등록
    void createPackaging(PackagingRequest request);

    // 전체 포장 정책 조회
    List<PackagingResponse> getPackagings();

    // 포장 정책 조회
    List<PackagingResponse> getEnabledPackagings();

    // 포장 정책 수정
    void modifyPackaging(Long packagingId, PackagingRequest request);

    // 포장 정책 삭제
    void deletePackaging(Long packagingId);
}
