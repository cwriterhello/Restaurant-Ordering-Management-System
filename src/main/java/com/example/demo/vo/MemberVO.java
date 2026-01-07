package com.example.demo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberVO {
    private Long id;
    private String phone;
    private String name;
    private String level; // NORMAL, SILVER, GOLD, PLATINUM
    private BigDecimal discount;
    private Integer points;
    private BigDecimal totalConsumption;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

