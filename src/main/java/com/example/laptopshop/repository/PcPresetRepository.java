package com.example.laptopshop.repository;

import com.example.laptopshop.entity.PcPreset;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PcPresetRepository extends JpaRepository<PcPreset, Long> {

    // Khách: chỉ lấy cấu hình đang bật, fetch luôn items + product để tránh N+1
    @EntityGraph(attributePaths = {"items", "items.product"})
    List<PcPreset> findByIsActiveTrueOrderByPresetIdDesc();

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<PcPreset> findBySlugAndIsActiveTrue(String slug);

    // Admin: lấy tất cả
    @EntityGraph(attributePaths = {"items", "items.product"})
    List<PcPreset> findAllByOrderByPresetIdDesc();

    boolean existsBySlug(String slug);
}