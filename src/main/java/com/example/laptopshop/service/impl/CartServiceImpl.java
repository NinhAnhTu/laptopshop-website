package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.Cart;
import com.example.laptopshop.entity.CartDetail;
import com.example.laptopshop.entity.Product;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.repository.CartDetailRepository;
import com.example.laptopshop.repository.CartRepository;
import com.example.laptopshop.repository.ProductRepository;
import com.example.laptopshop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void addToCart(User user, Long productId, int quantity) {
        // 1. Lấy giỏ hàng của user, nếu chưa có thì tạo mới
        Cart cart = cartRepository.findByUserUserId(user.getUserId());
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
        }

        // 2. Kiểm tra sản phẩm có tồn tại không
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // 3. Kiểm tra sản phẩm đã có trong giỏ chưa
        Optional<CartDetail> existingDetail = cartDetailRepository.findByCartCartIdAndProductProductId(cart.getCartId(), productId);

        if (existingDetail.isPresent()) {
            // Nếu có rồi -> Tăng số lượng
            CartDetail detail = existingDetail.get();
            detail.setQuantity(detail.getQuantity() + quantity);
            cartDetailRepository.save(detail);
        } else {
            // Nếu chưa có -> Tạo dòng mới
            CartDetail detail = new CartDetail();
            detail.setCart(cart);
            detail.setProduct(product);
            detail.setQuantity(quantity);
            cartDetailRepository.save(detail);
        }
    }

    @Override
    public Cart getCartByUser(User user) {
        return cartRepository.findByUserUserId(user.getUserId());
    }

    @Override
    @Transactional
    public void removeFromCart(Long cartDetailId) {
        // 1. Tìm CartDetail cần xóa
        Optional<CartDetail> cartDetailOptional = cartDetailRepository.findById(cartDetailId);

        if (cartDetailOptional.isPresent()) {
            CartDetail cartDetail = cartDetailOptional.get();
            Cart cart = cartDetail.getCart();

            // 2.Xóa nó khỏi danh sách của Cart (trong bộ nhớ)
            if (cart != null && cart.getCartDetails() != null) {
                cart.getCartDetails().remove(cartDetail);
            }

            // 3. Xóa trong Database
            cartDetailRepository.delete(cartDetail);
        }
    }

    @Override
    public void updateQuantity(Long cartDetailId, int quantity) {
        CartDetail detail = cartDetailRepository.findById(cartDetailId).orElse(null);
        if (detail != null) {
            // [MỚI] Kiểm tra tồn kho
            // Nếu số lượng khách muốn mua > số lượng trong kho -> Báo lỗi
            int currentStock = detail.getProduct().getStock();
            if (quantity > currentStock) {
                throw new RuntimeException("Xin lỗi, trong kho chỉ còn " + currentStock + " sản phẩm này.");
            }

            // Nếu đủ hàng thì cập nhật bình thường
            detail.setQuantity(quantity);
            cartDetailRepository.save(detail);
        }
    }

    @Override
    public int countItemsInCart(User user) {
        Cart cart = cartRepository.findByUserUserId(user.getUserId());
        if (cart == null || cart.getCartDetails() == null) return 0;
        return cart.getCartDetails().stream().mapToInt(CartDetail::getQuantity).sum();
    }
}