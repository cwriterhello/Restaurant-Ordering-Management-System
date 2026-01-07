package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
public class DishDTO {
    private Long id;
    
    @NotBlank(message = "菜品名称不能为空")
    private String name;
    
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    private String description;
    
    private String image;
    
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;
    
    private BigDecimal originalPrice;
    
    @NotNull(message = "库存不能为空")
    private Integer stock;
    
    private String unit;
    
    private Integer isAvailable;
    
    private Integer isRecommend;
    
    private Integer sortOrder;
}

