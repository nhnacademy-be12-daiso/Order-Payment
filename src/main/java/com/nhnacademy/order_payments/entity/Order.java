package com.nhnacademy.order_payments.entity;

import com.nhnacademy.order_payments.model.Grade;
import com.nhnacademy.order_payments.model.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = true)
    private GuestOrderer guestOrderer;

    @Setter
    @Column(name = "user_created_id", nullable = true)
    private Long userId;

    @Column(name = "order_number", unique = true)
    private long orderNumber; // pk랑 무슨 차이가 있는지?

    @Setter
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;// enum으로 독립

    @Column(name = "order_date")
    private ZonedDateTime orderDate;

    @Column(name = "orderer_name", nullable = false)
    private String ordererName;

    @Column(name = "total_price")
    private Integer totalPrice;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "grade_name")
    private Grade grade;

    public Order(long orderNumber, String ordererName, int totalPrice, String phoneNumber, String email, Grade grade){
        this.orderNumber = orderNumber;
        this.ordererName = ordererName;
        this.totalPrice = totalPrice;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.grade = grade;
        this.orderDate = ZonedDateTime.now();
    }

    @Setter
    @OneToMany(mappedBy = "oder")
    private List<OrderDetail> detailList;

    @Setter
    @OneToOne(mappedBy = "order")
    private Delivery delivery;
}
