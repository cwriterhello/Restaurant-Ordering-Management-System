package com.example.demo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TableVO {
    private Long id;
    private String tableNumber;
    private String qrCode;
    private Integer capacity;
    private String status; // EMPTY, OCCUPIED, RESERVED
    private Long currentOrderId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

