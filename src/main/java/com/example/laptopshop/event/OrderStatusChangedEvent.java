package com.example.laptopshop.event;

import com.example.laptopshop.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderStatusChangedEvent extends ApplicationEvent {
    private final Order order;
    private final String oldStatus;
    private final String newStatus;

    public OrderStatusChangedEvent(Object source, Order order, String oldStatus, String newStatus) {
        super(source);
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}