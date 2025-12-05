package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "DeliveryOrderDetails")
public class DeliveryOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delovery_order_detail_id")
    private long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "delovery_detail_id")
    private DeliveryDetail deliveryDetail;

    @Setter
    @ManyToOne
    @JoinColumn(name = "order_detail_id")
    private OrderDetail orderDetail;

    @Setter
    private int quantity;

    public DeliveryOrderDetail(int quantity){
        this(null, null, quantity);
    }
    public DeliveryOrderDetail(DeliveryDetail deliveryDetail, OrderDetail orderDetail, int quantity){
        this.deliveryDetail = deliveryDetail;
        this.orderDetail = orderDetail;
        this.quantity = quantity;
    }
}
