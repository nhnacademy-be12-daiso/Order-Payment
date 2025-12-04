package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.entity.Packaging;
import com.nhnacademy.order_payments.repository.PackagingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PackagingService {

    private final PackagingRepository packagingRepository;

    @Transactional
    public String getPackagingName(long id){
        Packaging packaging = packagingRepository.findPackagingById(id);
        if(packaging == null){
            return null;
        }
        return packaging.getName();
    }
}
