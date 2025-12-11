package com.nhnacademy.order_payments.service.delivery;

import com.nhnacademy.order_payments.repository.DeliveryDetailRepository;
import com.nhnacademy.order_payments.repository.DeliveryOrderDetailRepository;
import com.nhnacademy.order_payments.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryDetailRepository deliveryDetailRepository;
    private final DeliveryOrderDetailRepository deliveryOrderDetailRepository;
}
