package com.example.laptopshop.dto.request;

import lombok.Data;

@Data // Tự động sinh Getters và Setters
public class ShippingUpdateDTO {
    private String clientOrderCode;
    private String status;
}