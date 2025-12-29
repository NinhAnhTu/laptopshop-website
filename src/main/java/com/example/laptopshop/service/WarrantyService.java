package com.example.laptopshop.service;

import com.example.laptopshop.entity.Order;
import com.example.laptopshop.entity.Warranty;
import java.util.List;

public interface WarrantyService {

    // Lấy tất cả danh sách bảo hành
    List<Warranty> getAll();

    // Tìm kiếm bảo hành theo từ khóa
    List<Warranty> search(String keyword);

    // Kích hoạt bảo hành (tạo mới) khi đơn hàng thành công
    void activateWarranty(Order order);
    void scanAndExpireWarranties();
    void sendWarrantyExpiryReminders();
}