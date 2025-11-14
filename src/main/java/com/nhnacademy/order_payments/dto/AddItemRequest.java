package com.nhnacademy.order_payments.dto;


import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
public class AddItemRequest {

    private Long cartId;
    private Long bookId;
    private int quantity;

}
