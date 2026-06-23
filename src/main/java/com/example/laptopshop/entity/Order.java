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

    // =========================================================================
    // CODE ÁP DỤNG BUILDER PATTERN THỦ CÔNG
    // =========================================================================

    // 1. JPA/Hibernate LUÔN LUÔN yêu cầu một Constructor rỗng (Bắt buộc)
    public Order() {
    }

    // 2. Constructor private chỉ dành riêng cho Builder sử dụng
    private Order(OrderBuilder builder) {
        this.user = builder.user;
        this.status = builder.status;
        this.note = builder.note;
        this.shippingAddress = builder.shippingAddress;
        this.shippingName = builder.shippingName;
        this.shippingPhone = builder.shippingPhone;
        this.shippingCity = builder.shippingCity;
        this.totalProductsPrice = builder.totalProductsPrice;
        this.shippingFee = builder.shippingFee;
        this.discountAmount = builder.discountAmount;
        this.voucher = builder.voucher;
        this.finalAmount = builder.finalAmount;
        this.paymentMethod = builder.paymentMethod;
    }

    // 3. Hàm mồi để gọi Builder từ bên ngoài (Order.builder()...)
    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    // 4. Lớp Builder Nội Bộ (Static Inner Class)
    public static class OrderBuilder {
        // Khai báo lại các thuộc tính cần thiết để Build đơn hàng
        private User user;
        private String status;
        private String note;
        private String shippingAddress;
        private String shippingName;
        private String shippingPhone;
        private City shippingCity;
        private BigDecimal totalProductsPrice;
        private BigDecimal shippingFee;
        private BigDecimal discountAmount;
        private Voucher voucher;
        private BigDecimal finalAmount;
        private PaymentMethod paymentMethod;

        // Các hàm truyền tham số mang phong cách Fluent API (luôn return this)
        public OrderBuilder user(User user) {
            this.user = user;
            return this;
        }

        public OrderBuilder status(String status) {
            this.status = status;
            return this;
        }

        public OrderBuilder note(String note) {
            this.note = note;
            return this;
        }

        public OrderBuilder shippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
            return this;
        }

        public OrderBuilder shippingName(String shippingName) {
            this.shippingName = shippingName;
            return this;
        }

        public OrderBuilder shippingPhone(String shippingPhone) {
            this.shippingPhone = shippingPhone;
            return this;
        }

        public OrderBuilder shippingCity(City shippingCity) {
            this.shippingCity = shippingCity;
            return this;
        }

        public OrderBuilder totalProductsPrice(BigDecimal totalProductsPrice) {
            this.totalProductsPrice = totalProductsPrice;
            return this;
        }

        public OrderBuilder shippingFee(BigDecimal shippingFee) {
            this.shippingFee = shippingFee;
            return this;
        }

        public OrderBuilder discountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        public OrderBuilder voucher(Voucher voucher) {
            this.voucher = voucher;
            return this;
        }

        public OrderBuilder finalAmount(BigDecimal finalAmount) {
            this.finalAmount = finalAmount;
            return this;
        }

        public OrderBuilder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        // 5. Hàm quan trọng nhất: Khóa đối tượng và đóng gói!
        public Order build() {
            // [TÍNH NĂNG HAY]: Ở đây bạn có thể Validate dữ liệu trước khi cho phép tạo đơn
            if (this.status == null || this.status.trim().isEmpty()) {
                this.status = "Chờ xác nhận"; // Tự động gán mặc định nếu quên truyền status
            }
            if (this.finalAmount != null && this.finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Lỗi: Tổng tiền hóa đơn không được là số âm!");
            }

            // Nếu không có lỗi gì, trả về đối tượng Order thực sự
            return new Order(this);
        }
    }
}