package com.nhnacademy.order_payments.client;

import com.nhnacademy.order_payments.dto.order.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "TEAM3-USER", path = "/api/internal/users")
public interface UserApiClient {

    @GetMapping("/{userCreatedId}/info")
    ResponseEntity<UserInfoResponse> getUserInfo(@PathVariable("userCreatedId") Long userCreatedId);

}

