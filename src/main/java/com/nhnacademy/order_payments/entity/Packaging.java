package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Packagings")
@NoArgsConstructor
public class Packaging {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 이걸 그냥 Auto로 박아놓는게 맞나?
    @Column(name = "wrapping_paper_id")
    private Long wrappingPaperId;
    @Column(name = "wrapping_paper_name")
    private String wrappingPaperName;
    @Column(name = "wrapping_paper_price")
    private String wrappingPaperPrice;
}
