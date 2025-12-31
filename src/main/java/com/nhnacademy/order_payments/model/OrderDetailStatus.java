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

package com.nhnacademy.order_payments.model;

// 한 종류 책에 대한 배송 상태??????????????
public enum OrderDetailStatus {
    PENDING,    // 준비 중
    PREPARING,  // 준비 중
    SHIPPED,    // 배송 중
    DELIVERED,  // 배송 완료
    CANCELLED,  // 취소
    RETURNED,   // 반품 승인 상태
    RETURN_REQUESTED // 반품 신청 상태
}
