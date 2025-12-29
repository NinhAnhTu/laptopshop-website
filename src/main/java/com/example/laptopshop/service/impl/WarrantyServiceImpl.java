package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.*;
import com.example.laptopshop.repository.WarrantyRepository;
import com.example.laptopshop.service.EmailService;
import com.example.laptopshop.service.WarrantyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarrantyServiceImpl implements WarrantyService {

    private final WarrantyRepository warrantyRepository;
    private final EmailService emailService;

    @Override
    public List<Warranty> getAll() {
        return warrantyRepository.findAll();
    }
    @Override
    public List<Warranty> search(String keyword) {
        String searchKey = "%" + keyword + "%";

        return warrantyRepository.searchWarranty(searchKey);
    }

    @Override
    @Transactional
    public void activateWarranty(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = detail.getProduct();

            WarrantyPolicy policy = product.getWarrantyPolicy();

            if (policy != null) {
                for (int i = 0; i < detail.getQuantity(); i++) {
                    Warranty w = new Warranty();

                    String uniqueCode = "WAR-" + order.getOrderId() + "-"
                            + product.getProductId() + "-"
                            + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

                    w.setWarrantyCode(uniqueCode);
                    w.setUser(order.getUser());
                    w.setProduct(product);
                    w.setOrderDetail(detail);
                    w.setWarrantyPolicy(policy);

                    LocalDateTime now = LocalDateTime.now();
                    w.setPurchaseDate(now);
                    w.setExpirationDate(now.plusMonths(policy.getDurationMonths()));

                    w.setStatus("ACTIVE");

                    warrantyRepository.save(w);
                }
            }
        }
    }
    @Override
    public void scanAndExpireWarranties() {
        List<Warranty> activeWarranties = warrantyRepository.findByStatus("ACTIVE");
        LocalDateTime now = LocalDateTime.now();

        for (Warranty w : activeWarranties) {
            if (w.getExpirationDate().isBefore(now)) {
                w.setStatus("EXPIRED");
                warrantyRepository.save(w);
            }
        }
    }

    @Override
    public void sendWarrantyExpiryReminders() {
        System.out.println("--- BẮT ĐẦU QUÉT BẢO HÀNH SẮP HẾT HẠN ĐỂ GỬI MAIL ---");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);

        List<Warranty> list = warrantyRepository.findByExpirationDateBetweenAndStatus(now, sevenDaysLater, "ACTIVE");

        for (Warranty w : list) {
            System.out.println("Tìm thấy phiếu sắp hết hạn: " + w.getWarrantyCode());
            try {
                emailService.sendWarrantyExpirationEmail(w);
            } catch (Exception e) {
                System.out.println("Lỗi gửi mail: " + e.getMessage());
            }
        }
    }
}