package com.example.laptopshop.repository;

import com.example.laptopshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm user bằng email (dùng cho đăng nhập)
    Optional<User> findByEmail(String email);

    // Kiểm tra email đã tồn tại chưa (dùng khi đăng ký)
    boolean existsByEmail(String email);

    // Tìm user bằng token reset password (dùng khi quên mật khẩu)
    Optional<User> findByResetPasswordToken(String token);

    //Hàm tìm kiếm người dùng nâng cao
    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " u.fullname LIKE CONCAT('%', :keyword, '%') OR " +
            " u.email LIKE CONCAT('%', :keyword, '%') OR " +
            " u.phone LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:roleId IS NULL OR u.userType.userTypeId = :roleId)")
    List<User> searchUsers(@Param("keyword") String keyword,
                           @Param("roleId") Long roleId);
}