package com.example.laptopshop.repository;

import com.example.laptopshop.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTypeRepository extends JpaRepository<UserType, Long> {
    UserType findByTypeName(String typeName);
}