package com.example.laptopshop.repository;

import com.example.laptopshop.entity.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {
    // Tìm item trong giỏ hàng (để check trùng khi add)
    Optional<CartDetail> findByCartCartIdAndProductProductId(Long cartId, Long productId);

    // Lấy hết item trong giỏ
    List<CartDetail> findByCartCartId(Long cartId);

    // Xóa hết item trong giỏ (khi đặt hàng xong)
    void deleteByCartCartId(Long cartId);
}