package com.example.laptopshop.repository;

import com.example.laptopshop.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderOrderId(Long orderId);
    boolean existsByOrderUserUserIdAndProductProductIdAndOrderStatus(Long userId, Long productId, String status);
}