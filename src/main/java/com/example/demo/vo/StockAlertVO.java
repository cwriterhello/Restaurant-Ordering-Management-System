package com.example.demo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockAlertVO {
    private Long id;
    private Long dishId;
    private String dishName;
    private Integer currentStock;
    private Integer alertThreshold;
    private String status; // ALERT, RESOLVED
    private LocalDateTime createTime;
    private LocalDateTime resolveTime;
}

