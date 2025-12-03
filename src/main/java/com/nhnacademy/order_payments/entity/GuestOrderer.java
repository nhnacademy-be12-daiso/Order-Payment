package com.nhnacademy.order_payments.entity;

import com.nhnacademy.order_payments.model.Grade;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
@Table(name = "GuestsOrderers")
public class GuestOrderer {

    @Id
    @Column(name = "guest_id")
    private long id;

    @OneToMany(mappedBy = "guestOrderer")
    @Column(name = "order_id")
    private List<Order> orderList;
}
