package com.example.demo.service;

import com.example.demo.entity.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 发送订单到后厨
    public void sendOrderToKitchen(Order order) {
        try {
            messagingTemplate.convertAndSend("/topic/kitchen/orders", order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 发送订单状态更新
    public void sendOrderStatusUpdate(Order order) {
        try {
            messagingTemplate.convertAndSend("/topic/order/status/" + order.getId(), order);
            messagingTemplate.convertAndSend("/topic/kitchen/orders", order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 发送库存预警
    public void sendStockAlert(String message) {
        try {
            messagingTemplate.convertAndSend("/topic/admin/stock-alerts", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

