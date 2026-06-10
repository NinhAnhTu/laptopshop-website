package com.example.laptopshop.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ImportReceiptDetailDTO {
    private Long productId;
    private Integer quantity;
    private BigDecimal importPrice;
    private String serialList; // Nhận chuỗi mã Serial từ Textarea (cách nhau bằng dấu phẩy hoặc xuống dòng)
}