package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class TableDTO {
    private Long id;
    
    @NotBlank(message = "桌号不能为空")
    private String tableNumber;
    
    private String qrCode;
    
    @NotNull(message = "容纳人数不能为空")
    private Integer capacity;
    
    private String status; // EMPTY, OCCUPIED, RESERVED
    
    private Long currentOrderId;
}

