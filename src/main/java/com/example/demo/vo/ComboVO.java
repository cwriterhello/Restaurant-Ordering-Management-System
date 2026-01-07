package com.example.demo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ComboVO {
    private Long id;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer isAvailable;
    private Integer sortOrder;
    private List<ComboDishVO> dishes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @Data
    public static class ComboDishVO {
        private Long dishId;
        private String dishName;
        private Integer quantity;
    }
}

