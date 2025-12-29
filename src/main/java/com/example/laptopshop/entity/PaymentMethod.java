package com.example.laptopshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.example.laptopshop.entity.Transaction;

import java.util.List;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long methodId;

    private String methodName;

    // Một phương thức thanh toán có thể dùng cho nhiều đơn hàng
    @OneToMany(mappedBy = "paymentMethod")
    @JsonIgnore
    private List<Order> orders;

    // Một phương thức thanh toán có thể dùng cho nhiều giao dịch
    @OneToMany(mappedBy = "paymentMethod")
    @JsonIgnore
    private List<Transaction> transactions;
}
