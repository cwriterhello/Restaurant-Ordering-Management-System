package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PaymentDTO {
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod; // CASH, ALIPAY, WECHAT, MEMBER
    
    private Long cashierId;
}

