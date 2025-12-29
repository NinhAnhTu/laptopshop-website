package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.User;
import com.example.laptopshop.entity.UserType;
import com.example.laptopshop.repository.UserRepository;
import com.example.laptopshop.repository.UserTypeRepository;
import com.example.laptopshop.service.EmailService;
import com.example.laptopshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Triển khai hàm đổi mật khẩu
    @Override
    public void changeClientPassword(User currentUser, String oldPassword, String newPassword) {
        if (currentUser.getPassword() != null && !currentUser.getPassword().isEmpty()) {
            if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
                throw new RuntimeException("Mật khẩu hiện tại không chính xác!");
            }
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);

        emailService.sendNewPasswordEmail(currentUser.getEmail(), newPassword);
    }

    // --- 1. ĐĂNG KÝ USER MỚI (CLIENT) ---
    @Override
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Mặc định là Customer
        UserType customerRole = userTypeRepository.findByTypeName("Customer");
        if (customerRole == null) {
            customerRole = userTypeRepository.findById(2L).orElse(null);
        }
        user.setUserType(customerRole);

        return userRepository.save(user);
    }

    // --- 2. TÌM KIẾM CƠ BẢN ---
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    // --- 3. CẬP NHẬT PROFILE (CLIENT) ---
    @Override
    public void updateUser(User user) {
        User currentUser = userRepository.findById(user.getUserId()).orElse(null);
        if (currentUser != null) {
            currentUser.setFullname(user.getFullname());
            currentUser.setPhone(user.getPhone());
            currentUser.setAddress(user.getAddress());
            userRepository.save(currentUser);
        }
    }

    // --- 4. QUÊN MẬT KHẨU ---
    @Override
    public void generateResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        emailService.sendResetPasswordEmail(user.getEmail(), token);
    }

    @Override
    public User getByResetToken(String token) {
        return userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Đường dẫn không hợp lệ hoặc đã hết hạn!"));
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    // =================================================================
    // HÀM DÀNH CHO ADMIN QUẢN LÝ USER
    // =================================================================

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User saveUser(User user) {
        if (user.getUserId() == null) {
            String rawPassword = user.getPassword(); // Lấy mật khẩu gốc
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        else {
            User existingUser = userRepository.findById(user.getUserId()).orElse(null);
            if (existingUser != null) {
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    String rawPassword = user.getPassword(); // Lấy mật khẩu admin vừa nhập

                    // Gửi mail cho email cũ (trong DB) để đảm bảo an toàn
                    try {
                        emailService.sendNewPasswordEmail(existingUser.getEmail(), rawPassword);
                    } catch (Exception e) {
                        System.out.println("Lỗi gửi mail khi đổi pass admin: " + e.getMessage());
                    }

                    // Sau khi gửi xong thì mới mã hóa để lưu DB
                    user.setPassword(passwordEncoder.encode(rawPassword));
                } else {
                    // Nếu admin bỏ trống ô mật khẩu -> Giữ nguyên mật khẩu cũ
                    user.setPassword(existingUser.getPassword());
                }

                // Giữ lại các thông tin quan trọng khác
                if (user.getAvatarUrl() == null) user.setAvatarUrl(existingUser.getAvatarUrl());
                if (user.getProvider() == null) user.setProvider(existingUser.getProvider());
                if (user.getGoogleId() == null) user.setGoogleId(existingUser.getGoogleId());

                // Giữ nguyên ngày tạo
                user.setCreatedAt(existingUser.getCreatedAt());
            }
        }
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserType> getAllUserTypes() {
        return userTypeRepository.findAll();
    }
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);    }
    // Triển khai hàm tìm kiếm
    @Override
    public List<User> searchUsers(String keyword, Long roleId) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        return userRepository.searchUsers(keyword, roleId);
    }
}