package com.example.laptopshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true)
    private String username;

    private String password;
    private String fullname;
    private Boolean gender;
    private String phone;

    @Column(unique = true)
    private String email;

    @Column(name = "auth_provider")
    private String provider;

    private String googleId;
    private String avatarUrl;

    private String resetPasswordToken;
    private LocalDateTime resetTokenExpiry;

    private String address;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;

    @ManyToOne
    @JoinColumn(name = "user_type_id")
    private UserType userType;

    @OneToOne(mappedBy = "user")
    @JsonIgnore
    private Cart cart;
}