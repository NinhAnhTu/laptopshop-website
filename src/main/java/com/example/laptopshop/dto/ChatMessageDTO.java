package com.example.laptopshop.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long receiverId; // ID người nhận
    private String content;  // Nội dung tin nhắn
}