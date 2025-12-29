package com.example.laptopshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voucherId;

    @Column(unique = true)
    private String code;

    private BigDecimal discountPercent;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    private Integer quantity;
    private String status; // active, expired

    // Một voucher áp dụng cho nhiều đơn hàng
    @OneToMany(mappedBy = "voucher")
    @JsonIgnore
    private List<Order> orders;
}
