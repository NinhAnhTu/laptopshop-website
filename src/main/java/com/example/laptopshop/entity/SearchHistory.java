package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "search_history")
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    private LocalDateTime searchTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}