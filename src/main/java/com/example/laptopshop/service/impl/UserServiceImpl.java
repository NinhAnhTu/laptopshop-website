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
        if (user.getGoogleId() == null) {
            if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
                throw new RuntimeException("Lỗi: Người dùng bắt buộc phải có số điện thoại!");
            }
            if (!user.getPhone().matches("^\\d{10}$")) {
                throw new RuntimeException("Lỗi: Số điện thoại phải bao gồm đúng 10 chữ số!");
            }
        }
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new RuntimeException("Lỗi: Email không hợp lệ (thiếu @)!");
        }

        // KIỂM TRA UNIQUE KHI ĐĂNG KÝ MỚI
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Lỗi: Email này đã được sử dụng!");
        }
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("Lỗi: Số điện thoại này đã được sử dụng!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

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
        if (user.getGoogleId() == null) {
            if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
                throw new RuntimeException("Lỗi: Người dùng bắt buộc phải có số điện thoại!");
            }
            if (!user.getPhone().matches("^\\d{10}$")) {
                throw new RuntimeException("Lỗi: Số điện thoại phải bao gồm đúng 10 chữ số!");
            }
        }

        // KIỂM TRA UNIQUE SĐT KHI CLIENT UPDATE PROFILE (Loại trừ chính mình)
        if (user.getPhone() != null && userRepository.existsByPhoneAndUserIdNot(user.getPhone(), user.getUserId())) {
            throw new RuntimeException("Lỗi: Số điện thoại này đã được sử dụng bởi tài khoản khác!");
        }

        User currentUser = userRepository.findById(user.getUserId()).orElse(null);
        if (currentUser != null) {
            currentUser.setFullname(user.getFullname());
            currentUser.setPhone(user.getPhone());
            currentUser.setAddress(user.getAddress());
            currentUser.setCity(user.getCity());
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
        if (user.getGoogleId() == null) {
            if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
                throw new RuntimeException("Lỗi: Người dùng bắt buộc phải có số điện thoại!");
            }
            if (!user.getPhone().matches("^\\d{10}$")) {
                throw new RuntimeException("Lỗi: Số điện thoại phải bao gồm đúng 10 chữ số!");
            }
        }
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new RuntimeException("Lỗi: Email không hợp lệ (thiếu @)!");
        }

        // BIỆN PHÁP CHẶN UNIQUE CHO ADMIN
        if (user.getUserId() == null) {
            // TRƯỜNG HỢP THÊM MỚI (Id chưa tồn tại)
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("Lỗi: Email này đã được sử dụng!");
            }
            if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
                throw new RuntimeException("Lỗi: Số điện thoại này đã được sử dụng!");
            }

            String rawPassword = user.getPassword();
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        else {
            // TRƯỜNG HỢP CHỈNH SỬA (Id đã tồn tại - Phải dùng hàm loại trừ Id hiện tại)
            if (userRepository.existsByEmailAndUserIdNot(user.getEmail(), user.getUserId())) {
                throw new RuntimeException("Lỗi: Email này đã được sử dụng bởi tài khoản khác!");
            }
            if (user.getPhone() != null && userRepository.existsByPhoneAndUserIdNot(user.getPhone(), user.getUserId())) {
                throw new RuntimeException("Lỗi: Số điện thoại này đã được sử dụng bởi tài khoản khác!");
            }

            User existingUser = userRepository.findById(user.getUserId()).orElse(null);
            if (existingUser != null) {
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    String rawPassword = user.getPassword();
                    try {
                        emailService.sendNewPasswordEmail(existingUser.getEmail(), rawPassword);
                    } catch (Exception e) {
                        System.out.println("Lỗi gửi mail khi đổi pass admin: " + e.getMessage());
                    }
                    user.setPassword(passwordEncoder.encode(rawPassword));
                } else {
                    user.setPassword(existingUser.getPassword());
                }

                if (user.getAvatarUrl() == null) user.setAvatarUrl(existingUser.getAvatarUrl());
                if (user.getProvider() == null) user.setProvider(existingUser.getProvider());
                if (user.getGoogleId() == null) user.setGoogleId(existingUser.getGoogleId());
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