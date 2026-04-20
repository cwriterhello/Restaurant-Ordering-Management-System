package com.example.demo.dto;

import lombok.Data;
import java.util.List;

/**
 * 套餐菜品配置类
 * 用于配置套餐中包含的菜品及其数量
 */
@Data
public class ComboDishConfigDTO {
    
    /**
     * 套餐ID
     */
    private Long comboId;
    
    /**
     * 菜品列表
     */
    private List<ComboDishItemDTO> dishes;
    
    /**
     * 套餐菜品项
     */
    @Data
    public static class ComboDishItemDTO {
        
        /**
         * 菜品ID
         */
        private Long dishId;
        
        /**
         * 数量，默认为1
         */
        private Integer quantity;
    }
}