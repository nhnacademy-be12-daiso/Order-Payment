package com.nhnacademy.order_payments.service.order;

import com.nhnacademy.order_payments.entity.GuestOrderer;
import com.nhnacademy.order_payments.repository.GuestOrdererRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuestOrdererService {
    private final GuestOrdererRepository guestOrdererRepository;

    public GuestOrderer getOrderer(long userId){
        return guestOrdererRepository.findGuestOrdererById(userId);
    }
}
