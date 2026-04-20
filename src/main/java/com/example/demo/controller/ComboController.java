package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ComboAndDishesRequest;
import com.example.demo.dto.ComboDishConfigDTO;
import com.example.demo.entity.Combo;
import com.example.demo.service.ComboService;
import com.example.demo.vo.ComboVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 套餐管理控制器
 * 提供套餐的增删改查及套餐与菜品关联关系的管理功能
 */
@RestController
@RequestMapping("/api/combos")
@CrossOrigin(origins = "*")
public class ComboController {
    
    @Autowired
    private ComboService comboService;

    /**
     * 查询所有可用的套餐
     * @return 套餐列表
     */
    @GetMapping
    public ApiResponse<List<Combo>> getAllCombos() {
        try {
            List<Combo> combos = comboService.getAvailableCombos();
            return ApiResponse.success(combos);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据套餐ID查询对应的菜品
     * @param id 套餐ID
     * @return 包含菜品信息的套餐详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ComboVO> getComboWithDishes(@PathVariable Long id) {
        try {
            ComboVO combo = comboService.getComboWithDishes(id);
            return ApiResponse.success(combo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建套餐和菜品之间的关联关系
     * @param request 包含套餐信息和菜品配置的请求对象
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse<ComboVO> createComboWithRelation(@RequestBody ComboAndDishesRequest request) {
        try {
            Combo combo = request.getCombo();
            ComboDishConfigDTO dishesConfig = request.getDishesConfig();
            
            // 先保存套餐
            Combo savedCombo = comboService.saveCombo(combo);
            
            // 再配置菜品关联关系
            if (dishesConfig != null && dishesConfig.getDishes() != null && !dishesConfig.getDishes().isEmpty()) {
                dishesConfig.setComboId(savedCombo.getId());
                comboService.configureComboDishes(dishesConfig);
            }
            
            // 返回完整的套餐信息
            ComboVO comboVO = comboService.getComboWithDishes(savedCombo.getId());
            return ApiResponse.success("套餐及菜品配置成功", comboVO);
        } catch (Exception e) {
            return ApiResponse.error("套餐创建失败: " + e.getMessage());
        }
    }
}