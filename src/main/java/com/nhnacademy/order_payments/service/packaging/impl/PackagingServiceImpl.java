/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2025. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.order_payments.service.packaging.impl;

import com.nhnacademy.order_payments.dto.packaging.request.PackagingRequest;
import com.nhnacademy.order_payments.dto.packaging.response.PackagingResponse;
import com.nhnacademy.order_payments.entity.Packaging;
import com.nhnacademy.order_payments.repository.PackagingRepository;
import com.nhnacademy.order_payments.service.packaging.PackagingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class PackagingServiceImpl implements PackagingService {

    private final PackagingRepository packagingRepository;

    @Override
    @Transactional(readOnly = true)
    public String getPackagingName(Long packagingId) {
        if (packagingId == null) {
            return null;
        }

        return packagingRepository.findById(packagingId)
                .map(Packaging::getPackagingName)
                .orElse(null);
    }

    @Override
    public void createPackaging(PackagingRequest request) {
        Packaging packaging = new Packaging(
                request.packagingName(),
                request.price(),
                request.enabled()
        );

        packagingRepository.save(packaging);

        log.info("포장 정책 추가 - name: {}, price: {}, enabled: {}",
                request.packagingName(), request.price(), request.enabled());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackagingResponse> getPackagings() {
        return packagingRepository.findAll().stream()
                .map(p -> new PackagingResponse(
                        p.getPackagingId(),
                        p.getPackagingName(),
                        p.getPrice(),
                        p.getEnabled()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackagingResponse> getEnabledPackagings() {
        return packagingRepository.findAllByEnabled(true).stream()
                .map(p -> new PackagingResponse(
                        p.getPackagingId(),
                        p.getPackagingName(),
                        p.getPrice(),
                        p.getEnabled()
                ))
                .toList();
    }

    @Override
    public void modifyPackaging(Long packagingId, PackagingRequest request) {
        Packaging packaging = packagingRepository.findById(packagingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 포장 정책입니다."));

        packaging.modifyPackaging(
                request.packagingName(),
                request.price(),
                request.enabled()
        );

        log.info("포장 정책 수정 - id: {}, name: {}, price: {}, enabled: {}",
                packagingId, request.packagingName(), request.price(), request.enabled());
    }

    @Override
    public void deletePackaging(Long packagingId) {
        if (!packagingRepository.existsById(packagingId)) {
            throw new IllegalArgumentException("존재하지 않는 포장 정책입니다.");
        }

        packagingRepository.deleteById(packagingId);

        log.info("포장 정책 삭제 - id: {}", packagingId);
    }
}
