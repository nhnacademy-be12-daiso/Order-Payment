package com.nhnacademy.order_payments.dto.order;

public record InternalAddressResponse(String addressName,
                                      String zipCode,
                                      String roadAddress,
                                      String addressDetail
) {}