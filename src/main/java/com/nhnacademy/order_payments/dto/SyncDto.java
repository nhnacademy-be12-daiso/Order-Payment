package com.nhnacademy.order_payments.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SyncDto {
    List<SyncInfo> syncInfos;
}
