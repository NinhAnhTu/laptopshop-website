package com.example.laptopshop.service;

import com.example.laptopshop.entity.Cart;
import com.example.laptopshop.entity.User;

public interface CartService {
    // Thêm sản phẩm vào giỏ
    void addToCart(User user, Long productId, int quantity);

    // Lấy giỏ hàng của User
    Cart getCartByUser(User user);

    // Xóa sản phẩm khỏi giỏ
    void removeFromCart(Long cartDetailId);

    // Cập nhật số lượng (dùng khi khách chỉnh số lượng trong giỏ)
    void updateQuantity(Long cartDetailId, int quantity);

    // Đếm tổng số lượng sản phẩm (để hiện lên icon giỏ hàng header)
    int countItemsInCart(User user);
}