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

// '배송' 정보 DTO
public record DeliverySummaryDto(
        String receiverName, // 배송받을 사람 이름
        String receiverPhoneNumber, // 배송 받을 사람 연락처
        String postalCode, // 우편 번호
        String deliveryAddress, // 도로명 주소
        String deliveryAddressDetail, // 상세 주소
        String deliveryRequest, // 배송 요청사항
        String deliveryDate, // 희망 배송 날짜
        Long DeliveryFee // 배송비
) {
}