package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table
public class DeliveryDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_detail_id")
    private long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Setter
    @Column(name = "delivery_company_name")
    private String deliveryCompanyName;

    @Setter
    @Column(name = "delivery_man_name")
    private String deliveryManName;

    @Setter
    @Column(name = "estimated_at")
    private LocalDate estimatedAt; //배성 예정일이기에 일까지만

    @Setter
    @Column(name = "complete_at")
    private ZonedDateTime completeAt; //실제 도착한것이기에 시간까지

    @Setter
    @OneToMany(mappedBy = "deliveryDetail")
    private List<OrderDetail> orderDetailList;
}
