package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.DishDTO;
import com.example.demo.service.DishService;
import com.example.demo.vo.DishVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dishes")
@CrossOrigin(origins = "*")
public class DishController {
    @Autowired
    private DishService dishService;
    
    @GetMapping
    public ApiResponse<List<DishVO>> getAvailableDishes() {
        try {
            List<DishVO> dishes = dishService.getAvailableDishes();
            return ApiResponse.success(dishes);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // 为管理员提供获取所有菜品的端点
    @GetMapping("/all")
    public ApiResponse<List<DishVO>> getAllDishes() {
        try {
            List<DishVO> dishes = dishService.getAllDishes();
            return ApiResponse.success(dishes);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<DishVO>> getDishesByCategory(@PathVariable Long categoryId) {
        try {
            List<DishVO> dishes = dishService.getDishesByCategory(categoryId);
            return ApiResponse.success(dishes);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/recommend")
    public ApiResponse<List<DishVO>> getRecommendDishes() {
        try {
            List<DishVO> dishes = dishService.getRecommendDishes();
            return ApiResponse.success(dishes);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<DishVO> getDishById(@PathVariable Long id) {
        try {
            DishVO dish = dishService.getDishById(id);
            return ApiResponse.success(dish);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<DishVO> createDish(@Valid @RequestBody DishDTO dishDTO) {
        try {
            DishVO savedDish = dishService.saveDish(dishDTO);
            return ApiResponse.success("菜品创建成功", savedDish);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<DishVO> updateDish(@PathVariable Long id, @Valid @RequestBody DishDTO dishDTO) {
        try {
            dishDTO.setId(id);
            DishVO updatedDish = dishService.saveDish(dishDTO);
            return ApiResponse.success("菜品更新成功", updatedDish);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/status")
    public ApiResponse<DishVO> updateDishStatus(
            @PathVariable Long id,
            @RequestParam Integer isAvailable) {
        try {
            DishVO dish = dishService.getDishById(id);
            DishDTO dishDTO = new DishDTO();
            dishDTO.setId(id);
            dishDTO.setIsAvailable(isAvailable);
            DishVO updatedDish = dishService.saveDish(dishDTO);
            return ApiResponse.success("菜品状态更新成功", updatedDish);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDish(@PathVariable Long id) {
        try {
            dishService.deleteDish(id);
            return ApiResponse.success("菜品删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}