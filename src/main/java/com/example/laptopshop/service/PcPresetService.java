package com.example.laptopshop.service;

import com.example.laptopshop.dto.request.PcPresetCreateDTO;
import com.example.laptopshop.entity.PcPreset;
import com.example.laptopshop.entity.PcPresetItem;
import com.example.laptopshop.entity.Product;
import com.example.laptopshop.repository.PcPresetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PcPresetService {

    private final PcPresetRepository presetRepository;
    private final ProductService productService;

    // ===== PHÍA KHÁCH =====

    @Transactional(readOnly = true)
    public List<PcPreset> getActivePresets() {
        return presetRepository.findByIsActiveTrueOrderByPresetIdDesc();
    }

    @Transactional(readOnly = true)
    public Optional<PcPreset> getActiveBySlug(String slug) {
        return presetRepository.findBySlugAndIsActiveTrue(slug);
    }

    // ===== PHÍA ADMIN =====

    @Transactional(readOnly = true)
    public List<PcPreset> getAllPresets() {
        return presetRepository.findAllByOrderByPresetIdDesc();
    }

    @Transactional(readOnly = true)
    public PcPreset getById(Long id) {
        PcPreset preset = presetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cấu hình #" + id));
        preset.getItems().size(); // nạp items trong transaction
        return preset;
    }

    @Transactional
    public PcPreset createPreset(PcPresetCreateDTO dto) {
        validateBasicInfo(dto);
        List<ResolvedItem> resolved = resolveItems(dto);

        PcPreset preset = new PcPreset();
        applyBasicInfo(preset, dto);
        preset.setSlug(generateUniqueSlug(dto.getName()));
        for (ResolvedItem r : resolved) {
            preset.addItem(r.product(), r.quantity());
        }
        return presetRepository.save(preset);
    }

    @Transactional
    public PcPreset updatePreset(Long id, PcPresetCreateDTO dto) {
        validateBasicInfo(dto);
        List<ResolvedItem> resolved = resolveItems(dto);

        PcPreset preset = presetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cấu hình #" + id));
        applyBasicInfo(preset, dto); // giữ nguyên slug để link không bị đổi

        // Cập nhật theo kiểu so sánh (không xóa hết rồi thêm lại) để tránh vi phạm
        // UNIQUE(preset_id, product_id), vì Hibernate INSERT trước khi DELETE.
        Set<Long> wantedIds = new HashSet<>();
        for (ResolvedItem r : resolved) wantedIds.add(r.product().getProductId());

        preset.getItems().removeIf(it -> !wantedIds.contains(it.getProduct().getProductId()));

        Map<Long, PcPresetItem> existing = new HashMap<>();
        for (PcPresetItem it : preset.getItems()) existing.put(it.getProduct().getProductId(), it);

        for (ResolvedItem r : resolved) {
            PcPresetItem current = existing.get(r.product().getProductId());
            if (current != null) {
                current.setQuantity(r.quantity());
            } else {
                preset.addItem(r.product(), r.quantity());
            }
        }
        return presetRepository.save(preset);
    }

    @Transactional
    public void deletePreset(Long id) {
        presetRepository.deleteById(id); // items bị xóa theo (cascade)
    }

    @Transactional
    public boolean toggleActive(Long id) {
        PcPreset preset = presetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cấu hình #" + id));
        preset.setIsActive(!Boolean.TRUE.equals(preset.getIsActive()));
        return preset.getIsActive();
    }

    // ===== HÀM NỘI BỘ =====

    private record ResolvedItem(Product product, int quantity) {}

    private void validateBasicInfo(PcPresetCreateDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên cấu hình");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cấu hình phải có ít nhất 1 linh kiện");
        }
    }

    private void applyBasicInfo(PcPreset preset, PcPresetCreateDTO dto) {
        preset.setName(dto.getName().trim());
        preset.setDescription(blankToNull(dto.getDescription()));
        preset.setImageUrl(blankToNull(dto.getImageUrl()));
        preset.setTag(blankToNull(dto.getTag()));
        preset.setIsActive(dto.getIsActive() == null || dto.getIsActive());
    }

    /** Kiểm tra từng linh kiện, gộp các dòng trùng productId thành 1 (cộng dồn số lượng). */
    private List<ResolvedItem> resolveItems(PcPresetCreateDTO dto) {
        Map<Long, Integer> merged = new LinkedHashMap<>();
        for (PcPresetCreateDTO.ItemDTO it : dto.getItems()) {
            if (it == null || it.getProductId() == null) {
                throw new IllegalArgumentException("Có linh kiện không hợp lệ trong cấu hình");
            }
            int qty = it.getQuantity() == null ? 1 : it.getQuantity();
            if (qty < 1) {
                throw new IllegalArgumentException("Số lượng linh kiện phải từ 1 trở lên");
            }
            merged.merge(it.getProductId(), qty, Integer::sum);
        }

        List<ResolvedItem> result = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : merged.entrySet()) {
            Product product = loadProduct(e.getKey());
            if (product == null) {
                throw new IllegalArgumentException("Sản phẩm #" + e.getKey() + " không tồn tại");
            }
            if (!Boolean.TRUE.equals(product.getIsActive())) {
                throw new IllegalArgumentException("Sản phẩm \"" + product.getProductName() + "\" đang bị ẩn");
            }
            result.add(new ResolvedItem(product, e.getValue()));
        }
        return result;
    }

    private Product loadProduct(Long id) {
        try {
            return productService.getProductById(id);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String generateUniqueSlug(String name) {
        String base = toSlug(name);
        if (base.isEmpty()) base = "cau-hinh-pc";
        String slug = base;
        int i = 2;
        while (presetRepository.existsBySlug(slug)) {
            slug = base + "-" + i++;
        }
        return slug;
    }

    private String toSlug(String input) {
        String s = input.trim().replace('đ', 'd').replace('Đ', 'D');
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        return s.replaceAll("^-+|-+$", "");
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}