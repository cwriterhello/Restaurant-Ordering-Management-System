package com.example.demo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DailyStatisticsVO {
    private Long id;
    private LocalDate statDate;
    private Integer totalOrders;
    private BigDecimal totalAmount;
    private BigDecimal totalDiscount;
    private BigDecimal cashAmount;
    private BigDecimal alipayAmount;
    private BigDecimal wechatAmount;
    private BigDecimal memberAmount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

