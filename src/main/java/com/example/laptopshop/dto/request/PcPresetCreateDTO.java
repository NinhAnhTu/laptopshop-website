package com.example.laptopshop.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** Dữ liệu admin gửi lên (JSON) khi tạo / sửa cấu hình PC build sẵn. */
@Data
public class PcPresetCreateDTO {

    private String name;
    private String description;
    private String imageUrl;
    private String tag;
    private Boolean isActive = true;
    private List<ItemDTO> items = new ArrayList<>();

    @Data
    public static class ItemDTO {
        private Long productId;
        private Integer quantity = 1;
    }
}