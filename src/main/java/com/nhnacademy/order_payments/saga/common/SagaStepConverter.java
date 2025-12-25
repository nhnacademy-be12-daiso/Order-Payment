package com.nhnacademy.order_payments.saga.common;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter(autoApply = true)
public class SagaStepConverter implements AttributeConverter<SagaStep, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SagaStep attribute) {
        // Enum을 DB에 넣을 때: order 숫자값으로 변환
        return (attribute == null) ? null : attribute.getOrder();
    }

    @Override
    public SagaStep convertToEntityAttribute(Integer dbData) {
        // DB에서 꺼낼 때: 숫자를 가지고 그에 맞는 Enum 상수를 찾아줌
        if (dbData == null) return null;

        return Arrays.stream(SagaStep.values())
                .filter(step -> step.getOrder() == dbData)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 step 번호: " + dbData));
    }
}