package com.example.laptopshop.repository;

import com.example.laptopshop.entity.Order;
import com.example.laptopshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT MONTH(o.createdAt) as month, SUM(o.finalAmount) as revenue " +
            "FROM Order o WHERE YEAR(o.createdAt) = YEAR(CURRENT_DATE) AND o.status = 'Đã giao' " +
            "GROUP BY MONTH(o.createdAt)")
    List<Object[]> getMonthlyRevenue();

    @Query("SELECT o FROM Order o WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " CAST(o.orderId AS string) LIKE %:keyword% OR " +
            " o.user.fullname LIKE %:keyword% OR " +
            " o.user.phone LIKE %:keyword%) AND " +
            "(:status IS NULL OR :status = '' OR o.status = :status) AND " +
            "(:searchDate IS NULL OR DATE(o.createdAt) = :searchDate) " +
            "ORDER BY o.createdAt DESC")
    List<Order> searchOrders(@Param("keyword") String keyword,
                             @Param("status") String status,
                             @Param("searchDate") LocalDate searchDate);

    List<Order> findByUser_EmailOrderByCreatedAtDesc(String email);

    //Tính tổng tiền các đơn đã thành công (VNPAY hoặc COD đã giao)
    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.user = :user AND (o.status = 'Đã thanh toán' OR o.status = 'Đã giao')")
    BigDecimal sumTotalSpentByUser(@Param("user") User user);

    long countByStatus(String status);
}