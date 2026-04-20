package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 套餐实体类
 * 对应数据库中的combos表
 */
@TableName("combos")
@Data
public class Combo {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 套餐名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 套餐描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 图片URL
     */
    @TableField("image")
    private String image;
    
    /**
     * 套餐价格
     */
    @TableField("price")
    private BigDecimal price;
    
    /**
     * 原价（用于折扣显示）
     */
    @TableField("original_price")
    private BigDecimal originalPrice;
    
    /**
     * 是否上架：1-上架，0-下架
     */
    @TableField("is_available")
    private Integer isAvailable = 1;
    
    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder = 0;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

