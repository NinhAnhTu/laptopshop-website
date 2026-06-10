package com.example.laptopshop.service;

import com.example.laptopshop.dto.request.ImportReceiptDTO;
import com.example.laptopshop.entity.ImportReceipt;
import com.example.laptopshop.entity.User;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface ImportReceiptService {
    List<ImportReceipt> getAllReceipts();
    ImportReceipt getReceiptById(Long id);
    ImportReceipt saveImportReceipt(ImportReceiptDTO dto, User creator);
    Page<ImportReceipt> searchReceipts(Long supplierId, String productName, LocalDate startDate, LocalDate endDate, int page, int size);
}