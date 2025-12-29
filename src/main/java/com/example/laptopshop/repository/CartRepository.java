package com.example.laptopshop.repository;

import com.example.laptopshop.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Tìm giỏ hàng của user
    Cart findByUserUserId(Long userId);
}