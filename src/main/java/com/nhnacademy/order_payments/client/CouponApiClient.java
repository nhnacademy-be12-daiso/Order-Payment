package com.nhnacademy.order_payments.client;

import com.nhnacademy.order_payments.dto.order.CouponResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "TEAM3-COUPON")
public interface CouponApiClient {
    @GetMapping("/api/coupons/users/{userId}/available")
    ResponseEntity<List<CouponResponse>> getAvailableCoupons(@PathVariable Long userId);
}
