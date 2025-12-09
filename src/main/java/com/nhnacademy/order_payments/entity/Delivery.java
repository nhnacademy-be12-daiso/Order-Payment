package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private long id;

    @Setter
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Setter
    @Column(name = "deliver_address", nullable = false)
    private String address;

    @Setter
    @Column(name = "deliver_address_detail", nullable = false)
    private String addressDetail;

    @Setter
    @Column(name = "postal_code")
    private String postalCode;

    @Setter
    @Column(name = "receiver_name")
    private String receiverName;

    @Setter
    @Column(name = "receiver_phone_number")
    private String receiverPhoneNumber;

    @Column(name = "delivary_fee")
    private int fee;

    public Delivery(String address, String postalCode, String receiverName, String receiverPhoneNumber, int fee){
        this.address = address;
        this.postalCode = postalCode;
        this.receiverName = receiverName;
        this.receiverPhoneNumber = receiverPhoneNumber;
        this.fee = fee;
    }

    @Setter
    @OneToMany(mappedBy = "delivery")
    private List<DeliveryDetail> deliveryDetailList;
}
