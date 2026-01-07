package com.example.demo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItemVO {
    private Long id;
    private Long orderId;
    private String itemType; // DISH, COMBO
    private Long itemId;
    private String itemName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private String status; // PENDING, PREPARING, READY
    private LocalDateTime createTime;
}

