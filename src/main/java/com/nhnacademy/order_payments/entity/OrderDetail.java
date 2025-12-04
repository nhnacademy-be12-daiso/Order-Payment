package com.nhnacademy.order_payments.entity;

import com.nhnacademy.order_payments.model.OrderDetailStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "OrderDetails")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order; // 관계매핑 필요

    @Column(name = "book_id")
    private long bookId; // 도서에서 뽑아와야 함

    private int price;

    private int quantity;

    @Column(name = "packaging_id")
    private Long packagingId; // 저장만 함

    @Setter
    @Column(name = "order_detail_status")
    private OrderDetailStatus orderDetailStatus;

    public OrderDetail(long bookId, int price, int quantity, Long packagingId){
        this.bookId = bookId;
        this.price = price;
        this.quantity = quantity;
        this.packagingId = packagingId;
        this.orderDetailStatus = OrderDetailStatus.PENDING;
    }

//    @Setter
//    @ManyToOne
//    @JoinColumn(name = "deliver_detail_id")
//    private DeliveryDetail deliveryDetail;

    @Setter
    @OneToMany(mappedBy = "orderDetail")
    private List<DeliveryOrderDetail> deliveryOrderDetails;

}
