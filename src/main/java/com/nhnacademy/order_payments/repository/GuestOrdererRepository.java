package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.GuestOrderer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestOrdererRepository extends JpaRepository<GuestOrderer, Long> {
    GuestOrderer findGuestOrdererById(long id);
}
