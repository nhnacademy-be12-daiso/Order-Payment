package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "Packaging")
public class Packaging {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "packaging_id")
    private long id;

    @Column(name = "wrapping_paper_name")
    private String name;

    @Column(name = "wrapping_paper_price")
    private Long price;
}
