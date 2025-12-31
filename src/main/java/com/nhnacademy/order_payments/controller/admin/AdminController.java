package com.nhnacademy.order_payments.controller.admin;

import com.nhnacademy.order_payments.dto.order.AdminOrderDto;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/orders")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    List<AdminOrderDto> getAllOrders(@RequestHeader("X-User-Id") Long adminId) {
        List<Order> orders = adminService.getAllOrders();
        List<AdminOrderDto> orderResponses = orders.stream()
                .map(AdminOrderDto::new).toList();

        return orderResponses;
    }

    @PatchMapping("/{orderId}/status")
    void setTransit(@RequestHeader("X-User-Id") Long adminId,
                    @PathVariable Long orderId) {
        adminService.startDelivery(orderId);
        log.info("배송이 시작되었습니다. Order ID : {}", orderId);
    }
}
