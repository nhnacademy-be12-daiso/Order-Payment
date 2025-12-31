package com.nhnacademy.order_payments.controller.admin;

import com.nhnacademy.order_payments.dto.order.AdminOrderDto;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.model.AdminRefundOrderDto;
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

    @GetMapping("/refunds")
    List<AdminRefundOrderDto> getRefundOrders(@RequestHeader("X-User-Id") Long adminId) {
        List<OrderDetail> orderDetails = adminService.getRefundOrders();

        List<AdminRefundOrderDto> refundOrders = orderDetails.stream()
                .map(AdminRefundOrderDto::new).toList();

        return refundOrders;
    }

    @PatchMapping("/{orderId}/status")
    void setTransit(@RequestHeader("X-User-Id") Long adminId,
                    @PathVariable Long orderId) {
        adminService.startDelivery(orderId);
        log.info("상품이 출고되었습니다. Order ID : {}", orderId);
    }

    // 배송 완료로 상태 변경
    @PatchMapping("/{orderId}/complete")
    void setDelivered(@RequestHeader("X-User-Id") Long adminId,
                      @PathVariable Long orderId) {
        adminService.setDeliveryComplete(orderId);
        log.info("상품이 배송 완료 되었습니다. Order ID : {}", orderId);
    }

    @PatchMapping("/{orderDetailId}/refund/approve")
    void approveRefund(@RequestHeader("X-User-Id") Long adminId,
                       @PathVariable Long orderDetailId) {
        adminService.approveRefund(orderDetailId);
        log.info("상품의 반품이 승인되었습니다. OrderDetail ID : {}", orderDetailId);
    }

}
