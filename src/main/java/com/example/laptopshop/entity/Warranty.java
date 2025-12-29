// làm chức năng bảo hành.
package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "warranties")
@Getter
@Setter
public class Warranty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String warrantyCode;

    // Khách hàng nào sở hữu
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Bảo hành cho sản phẩm nào
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Thuộc đơn hàng nào
    @ManyToOne
    @JoinColumn(name = "order_detail_id")
    private OrderDetail orderDetail;

    // Liên kết với chính sách nào (Để biết quy định bảo hành)
    @ManyToOne
    @JoinColumn(name = "policy_id")
    private WarrantyPolicy warrantyPolicy;

    private LocalDateTime purchaseDate;   // Ngày kích hoạt (Ngày giao hàng thành công)
    private LocalDateTime expirationDate; // Ngày hết hạn (Ngày mua + số tháng trong Policy)

    private String status; // ACTIVE (Đang bảo hành), EXPIRED (Hết hạn), VOID (Từ chối BH)
}
