package com.nhnacademy.order_payments.service.order;

import com.nhnacademy.order_payments.entity.GuestOrderer;
import com.nhnacademy.order_payments.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderResultService {
    private final OrderRepository orderRepository;

    private final GuestOrdererService guestOrdererService;

    public List<Object> getOderList(long userId){
        GuestOrderer guestOrderer = guestOrdererService.getOrderer(userId);
        if(guestOrderer == null){

        } else {

        }
//        guestOrderer.getOrdererId()
        return List.of();
    }
}
