package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.Packaging;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackagingRepository extends JpaRepository<Packaging, Long> {
    Packaging findPackagingById(long id);
}
