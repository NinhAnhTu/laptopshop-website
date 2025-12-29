package com.example.laptopshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Điểm kết nối socket cho JS client
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Tiền tố cho các đường dẫn mà client gửi lên
        registry.setApplicationDestinationPrefixes("/app");
        // Tiền tố cho các đường dẫn mà client đăng ký lắng nghe (Subscribe)
        registry.enableSimpleBroker("/topic", "/queue");
    }
}