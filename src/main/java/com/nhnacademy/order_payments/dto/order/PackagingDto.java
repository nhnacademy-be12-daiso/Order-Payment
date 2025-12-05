package com.nhnacademy.order_payments.dto.order;

import com.nhnacademy.order_payments.entity.Packaging;
import jakarta.persistence.Column;

public record PackagingDto(
        long id,
        String name,
        int price
) {
    public PackagingDto(Packaging packaging) {
        this(
                packaging.getId(), packaging.getName(), packaging.getPrice()
        );
    }
}
