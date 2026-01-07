package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("dishes")
@Data
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("name")
    private String name;
    
    @TableField("category_id")
    private Long categoryId;
    
    @TableField("description")
    private String description;
    
    @TableField("image")
    private String image;
    
    @TableField("price")
    private BigDecimal price;
    
    @TableField("original_price")
    private BigDecimal originalPrice;
    
    @TableField("stock")
    private Integer stock = 0;
    
    @TableField("unit")
    private String unit = "份";
    
    @TableField("is_available")
    private Integer isAvailable = 1; // 1-上架，0-下架
    
    @TableField("is_recommend")
    private Integer isRecommend = 0; // 1-推荐，0-否
    
    @TableField("sort_order")
    private Integer sortOrder = 0;
    
    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

