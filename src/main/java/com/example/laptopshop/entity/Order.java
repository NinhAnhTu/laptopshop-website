package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.example.laptopshop.entity.Transaction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String status;

    // Tiền
    private BigDecimal totalProductsPrice;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    // Snapshot địa chỉ giao hàng
    private String shippingAddress;
    private String shippingName;
    private String shippingPhone;

    @ManyToOne
    @JoinColumn(name = "shipping_city_id")
    private City shippingCity;

    private String note;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "order")
    private List<Transaction> transactions;
}
