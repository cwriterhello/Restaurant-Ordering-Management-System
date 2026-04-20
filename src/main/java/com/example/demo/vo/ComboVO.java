package com.example.demo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 套餐视图对象
 * 用于向前端返回套餐及其关联菜品的完整信息
 */
@Data
public class ComboVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 套餐名称
     */
    private String name;
    
    /**
     * 套餐描述
     */
    private String description;
    
    /**
     * 图片URL
     */
    private String image;
    
    /**
     * 套餐价格
     */
    private BigDecimal price;
    
    /**
     * 原价（用于折扣显示）
     */
    private BigDecimal originalPrice;
    
    /**
     * 是否上架：1-上架，0-下架
     */
    private Integer isAvailable;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 关联的菜品列表
     */
    private List<ComboDishVO> dishes;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 套餐菜品视图子类
     */
    @Data
    public static class ComboDishVO {
        
        /**
         * 菜品ID
         */
        private Long dishId;
        
        /**
         * 菜品名称
         */
        private String dishName;
        
        /**
         * 单价
         */
        private BigDecimal price;
        
        /**
         * 数量
         */
        private Integer quantity;
    }
}

