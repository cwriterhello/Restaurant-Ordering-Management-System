package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class OrderStatusUpdateDTO {
    @NotBlank(message = "订单状态不能为空")
    private String status; // PENDING, CONFIRMED, PREPARING, READY, PAID, CANCELLED
}

