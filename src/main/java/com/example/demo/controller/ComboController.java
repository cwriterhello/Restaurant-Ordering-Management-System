package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ComboDishConfigDTO;
import com.example.demo.entity.Combo;
import com.example.demo.service.ComboService;
import com.example.demo.vo.ComboVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/combos")
@CrossOrigin(origins = "*")
public class ComboController {
    @Autowired
    private ComboService comboService;
    
    @GetMapping
    public ApiResponse<List<Combo>> getAvailableCombos() {
        try {
            List<Combo> combos = comboService.getAvailableCombos();
            return ApiResponse.success(combos);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<ComboVO> getComboById(@PathVariable Long id) {
        try {
            ComboVO combo = comboService.getComboWithDishes(id);
            return ApiResponse.success(combo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<Combo> createCombo(@RequestBody Combo combo) {
        try {
            Combo savedCombo = comboService.saveCombo(combo);
            return ApiResponse.success("套餐创建成功", savedCombo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Combo> updateCombo(@PathVariable Long id, @RequestBody Combo combo) {
        try {
            combo.setId(id);
            Combo updatedCombo = comboService.saveCombo(combo);
            return ApiResponse.success("套餐更新成功", updatedCombo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/status")
    public ApiResponse<Combo> updateComboStatus(
            @PathVariable Long id,
            @RequestParam Integer isAvailable) {
        try {
            Combo combo = comboService.getComboById(id);
            combo.setIsAvailable(isAvailable);
            Combo updatedCombo = comboService.saveCombo(combo);
            return ApiResponse.success("套餐状态更新成功", updatedCombo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCombo(@PathVariable Long id) {
        try {
            comboService.deleteCombo(id);
            return ApiResponse.success("套餐删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/dishes")
    public ApiResponse<Void> configureComboDishes(@PathVariable Long id, @RequestBody ComboDishConfigDTO config) {
        try {
            config.setComboId(id);
            comboService.configureComboDishes(config);
            return ApiResponse.success("套餐菜品配置成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}