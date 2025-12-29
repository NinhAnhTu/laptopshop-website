package com.example.laptopshop.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    // Bean này giúp mã hóa mật khẩu (123456 -> $2a$10$D8...)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean này giúp copy dữ liệu từ DTO sang Entity và ngược lại
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}