package com.nhnacademy.order_payments.client;

import com.nhnacademy.order_payments.dto.order.CouponResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "TEAM3-COUPON")
public interface CouponApiClient {
    @GetMapping("/{userId}")
    CouponResponse getAvailableCoupons(@PathVariable Long userId);
}
