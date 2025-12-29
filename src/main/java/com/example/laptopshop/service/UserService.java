package com.example.laptopshop.service;

import com.example.laptopshop.entity.User;
import com.example.laptopshop.entity.UserType;
import java.util.List;

public interface UserService {
    User registerUser(User user);
    User findByEmail(String email);
    void save(User user);
    void updateUser(User user);
    void generateResetToken(String email);
    User getByResetToken(String token);
    void updatePassword(User user, String newPassword);

    // Admin features
    List<User> getAllUsers();
    User getUserById(Long id);
    User saveUser(User user);
    void deleteUser(Long id);
    List<UserType> getAllUserTypes();
    User getUserByEmail(String email);
    List<User> searchUsers(String keyword, Long roleId);
    void changeClientPassword(User currentUser, String oldPassword, String newPassword);
}