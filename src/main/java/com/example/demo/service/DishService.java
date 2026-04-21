package com.example.demo.service;

import com.example.demo.dto.DishDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.Dish;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.mapper.DishMapper;
import com.example.demo.util.BeanUtil;
import com.example.demo.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisService redisService;

    private static final String DISH_CACHE_KEY = "dish:";
    private static final String DISH_LIST_CACHE_KEY = "dish:list:";
    private static final long CACHE_EXPIRE_TIME = 30;

    public List<DishVO> getAvailableDishes() {
        String cacheKey = DISH_LIST_CACHE_KEY + "available";
        List<DishVO> cachedList = redisService.getList(cacheKey, DishVO.class);
        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList;
        }

        List<Dish> dishes = dishMapper.findByIsAvailableOrderBySortOrderAsc(1);
        List<DishVO> result = convertToVOList(dishes);
        redisService.setList(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        return result;
    }

    public List<DishVO> getAllDishes() {
        String cacheKey = DISH_LIST_CACHE_KEY + "all";
        List<DishVO> cachedList = redisService.getList(cacheKey, DishVO.class);
        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList;
        }

        List<Dish> dishes = dishMapper.selectList(null);
        List<DishVO> result = convertToVOList(dishes);
        redisService.setList(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        return result;
    }

    public List<DishVO> getDishesByCategory(Long categoryId) {
        String cacheKey = DISH_LIST_CACHE_KEY + "category:" + categoryId;
        List<DishVO> cachedList = redisService.getList(cacheKey, DishVO.class);
        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList;
        }

        List<Dish> dishes = dishMapper.findByCategoryIdAndIsAvailableOrderBySortOrderAsc(categoryId, 1);
        List<DishVO> result = convertToVOList(dishes);
        redisService.setList(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        return result;
    }

    public List<DishVO> getRecommendDishes() {
        String cacheKey = DISH_LIST_CACHE_KEY + "recommend";
        List<DishVO> cachedList = redisService.getList(cacheKey, DishVO.class);
        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList;
        }

        List<Dish> dishes = dishMapper.findByIsAvailableAndIsRecommendOrderBySortOrderAsc(1, 1);
        List<DishVO> result = convertToVOList(dishes);
        redisService.setList(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        return result;
    }

    public DishVO getDishById(Long id) {
        String cacheKey = DISH_CACHE_KEY + id;
        DishVO cachedDish = (DishVO) redisService.get(cacheKey);
        if (cachedDish != null) {
            return cachedDish;
        }

        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }
        DishVO result = convertToVO(dish);
        redisService.set(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        return result;
    }

    public DishVO saveDish(DishDTO dishDTO) {
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        if (dish.getId() == null) {
            dishMapper.insert(dish);
        } else {
            dishMapper.updateById(dish);
        }
        clearDishCache();
        return convertToVO(dish);
    }

    public void deleteDish(Long id) {
        dishMapper.deleteById(id);
        clearDishCache();
        redisService.delete(DISH_CACHE_KEY + id);
    }

    private void clearDishCache() {
        redisService.delete(DISH_LIST_CACHE_KEY + "available");
        redisService.delete(DISH_LIST_CACHE_KEY + "all");
        redisService.delete(DISH_LIST_CACHE_KEY + "recommend");
    }

    private DishVO convertToVO(Dish dish) {
        DishVO vo = BeanUtil.copyProperties(dish, DishVO.class);
        if (dish.getCategoryId() != null) {
            Category category = categoryMapper.selectById(dish.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        return vo;
    }

    private List<DishVO> convertToVOList(List<Dish> dishes) {
        if (dishes == null || dishes.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> categoryIds = dishes.stream()
                .map(Dish::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> categoryNameMap = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (left, right) -> left));

        return dishes.stream()
                .map(dish -> {
                    DishVO vo = BeanUtil.copyProperties(dish, DishVO.class);
                    if (dish.getCategoryId() != null) {
                        vo.setCategoryName(categoryNameMap.get(dish.getCategoryId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
