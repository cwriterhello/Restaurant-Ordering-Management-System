package com.example.demo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DishVO {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String description;
    private String image;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String unit;
    private Integer isAvailable;
    private Integer isRecommend;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

