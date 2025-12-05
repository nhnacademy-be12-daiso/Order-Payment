package com.nhnacademy.order_payments.client;

import com.nhnacademy.order_payments.dto.order.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "TEAM3-USER", path = "/api/users")
public interface UserApiClient {

    @GetMapping("/api/internal/users/{userCreatedId}/info")
    UserInfoResponse getUserInfo(@PathVariable Long userCreatedId);

}
