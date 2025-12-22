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

// 주문 한 건에 대해 도서가 많을 때 분리가 되잖아요
// 그 주문 각각에 대한 배송 상태
public enum DeliveryStatus {
    PICKED_UP,  // 가져감
    IN_TRANSIT,     // 배송중
    OUT_FOR_DELIVERY, // 이게 뭐야
    DELIVERED,  // 배송 완료
    FAILED_ATTEMPT  // 배송 실패
}
