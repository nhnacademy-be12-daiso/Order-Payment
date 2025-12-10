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

import com.nhnacademy.order_payments.model.Grade;
import com.nhnacademy.order_payments.model.OrderStatus;
import java.time.ZonedDateTime;

public record OrderResponse(
        Long orderId,
        Long orderNumber,
        OrderStatus orderStatus,
        ZonedDateTime orderDate,
        String ordererName,
        Integer totalPrice,
        String ordererPhoneNumber,
        String ordererEmail,
        Grade grade,

        DeliveryResponse delivery) {
}
