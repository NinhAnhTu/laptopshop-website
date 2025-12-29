package com.example.laptopshop.repository;

import com.example.laptopshop.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    // Sắp xếp theo ĐỘ PHỔ BIẾN (COUNT) trước, sau đó mới đến THỜI GIAN (MAX)
    @Query("SELECT s.keyword FROM SearchHistory s " +
            "WHERE s.user.userId = :userId " +
            "GROUP BY s.keyword " +
            "ORDER BY COUNT(s.id) DESC, MAX(s.searchTime) DESC")
    List<String> findRecentKeywords(Long userId);
}