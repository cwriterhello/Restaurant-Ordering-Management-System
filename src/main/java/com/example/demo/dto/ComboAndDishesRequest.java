package com.example.demo.dto;

import com.example.demo.entity.Combo;

/**
 * 套餐和菜品配置请求类
 * 用于创建套餐时同时配置关联的菜品
 */
public class ComboAndDishesRequest {
    
    /**
     * 套餐基本信息
     */
    private Combo combo;
    
    /**
     * 菜品配置信息
     */
    private ComboDishConfigDTO dishesConfig;

    public Combo getCombo() {
        return combo;
    }

    public void setCombo(Combo combo) {
        this.combo = combo;
    }

    public ComboDishConfigDTO getDishesConfig() {
        return dishesConfig;
    }

    public void setDishesConfig(ComboDishConfigDTO dishesConfig) {
        this.dishesConfig = dishesConfig;
    }
}