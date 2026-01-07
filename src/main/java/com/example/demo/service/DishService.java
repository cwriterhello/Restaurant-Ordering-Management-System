package com.example.demo.service;

import com.example.demo.dto.DishDTO;
import com.example.demo.entity.Dish;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.mapper.DishMapper;
import com.example.demo.util.BeanUtil;
import com.example.demo.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishService {
    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    public List<DishVO> getAvailableDishes() {
        List<Dish> dishes = dishMapper.findByIsAvailableOrderBySortOrderAsc(1);
        return convertToVOList(dishes);
    }
    
    // 为管理员提供获取所有菜品（包括下架）的方法
    public List<DishVO> getAllDishes() {
        List<Dish> dishes = dishMapper.selectList(null); // 查询所有菜品，不带任何条件
        return convertToVOList(dishes);
    }
    
    public List<DishVO> getDishesByCategory(Long categoryId) {
        List<Dish> dishes = dishMapper.findByCategoryIdAndIsAvailableOrderBySortOrderAsc(categoryId, 1);
        return convertToVOList(dishes);
    }
    
    public List<DishVO> getRecommendDishes() {
        List<Dish> dishes = dishMapper.findByIsAvailableAndIsRecommendOrderBySortOrderAsc(1, 1);
        return convertToVOList(dishes);
    }
    
    public DishVO getDishById(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }
        return convertToVO(dish);
    }
    
    public DishVO saveDish(DishDTO dishDTO) {
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        if (dish.getId() == null) {
            dishMapper.insert(dish);
        } else {
            dishMapper.updateById(dish);
        }
        return convertToVO(dish);
    }
    
    public void deleteDish(Long id) {
        dishMapper.deleteById(id);
    }
    
    private DishVO convertToVO(Dish dish) {
        DishVO vo = BeanUtil.copyProperties(dish, DishVO.class);
        if (dish.getCategoryId() != null) {
            var category = categoryMapper.selectById(dish.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        return vo;
    }
    
    private List<DishVO> convertToVOList(List<Dish> dishes) {
        return dishes.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
}