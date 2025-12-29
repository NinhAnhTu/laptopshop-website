package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "user_type")
@Getter
@Setter
public class UserType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userTypeId;

    private String typeName; // Admin, Customer

    @OneToMany(mappedBy = "userType")
    @JsonIgnore
    private List<User> users;
}