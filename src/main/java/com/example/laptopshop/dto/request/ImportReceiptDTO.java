package com.example.laptopshop.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ImportReceiptDTO {
    private Long supplierId;
    private String note;
    private List<ImportReceiptDetailDTO> items; // Danh sách các sản phẩm nhập trong phiếu này
}