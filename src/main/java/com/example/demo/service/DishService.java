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
    
    // Redis缓存键前缀
    private static final String DISH_CACHE_KEY = "dish:";
    private static final String DISH_LIST_CACHE_KEY = "dish:list:";
    private static final long CACHE_EXPIRE_TIME = 30; // 缓存过期时间（分钟）
    
    /**
     * 获取上架状态的菜品列表（带缓存）
     */
    public List<DishVO> getAvailableDishes() {
        String cacheKey = DISH_LIST_CACHE_KEY + "available";
        
        // 先从缓存中获取
        if (redisService.hasKey(cacheKey)) {
            List<DishVO> cachedList = redisService.getList(cacheKey, DishVO.class);
            if (cachedList != null && !cachedList.isEmpty()) {
                return cachedList;
            }
        }
        
        // 缓存未命中，从数据库查询
        List<Dish> dishes = dishMapper.findByIsAvailableOrderBySortOrderAsc(1);
        List<DishVO> result = convertToVOList(dishes);
        
        // 存入缓存
        redisService.setList(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        
        return result;
    }
    
    /**
     * 获取所有菜品（包括下架）（带缓存）
     */
    public List<DishVO> getAllDishes() {
        String cacheKey = DISH_LIST_CACHE_KEY + "all";
        
        // 先从缓存中获取
        if (redisService.hasKey(cacheKey)) {
            List<DishVO> cachedList = redisService.getList(cacheKey, DishVO.class);
            if (cachedList != null && !cachedList.isEmpty()) {
                return cachedList;
            }
        }
        
        // 缓存未命中，从数据库查询
        List<Dish> dishes = dishMapper.selectList(null); // 查询所有菜品，不带任何条件
        List<DishVO> result = convertToVOList(dishes);
        
        // 存入缓存
        redisService.setList(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        
        return result;
    }
    
    /**
     * 按分类查询菜品（带缓存）
     */
    public List<DishVO> getDishesByCategory(Long categoryId) {
        String cacheKey = DISH_LIST_CACHE_KEY + "category:" + categoryId;
        
        // 先从缓存中获取
        if (redisService.hasKey(cacheKey)) {
            List<DishVO> cachedList = redisService.getList(cacheKey, DishVO.class);
            if (cachedList != null && !cachedList.isEmpty()) {
                return cachedList;
            }
        }
        
        // 缓存未命中，从数据库查询
        List<Dish> dishes = dishMapper.findByCategoryIdAndIsAvailableOrderBySortOrderAsc(categoryId, 1);
        List<DishVO> result = convertToVOList(dishes);
        
        // 存入缓存
        redisService.setList(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        
        return result;
    }
    
    /**
     * 获取推荐菜品（带缓存）
     */
    public List<DishVO> getRecommendDishes() {
        String cacheKey = DISH_LIST_CACHE_KEY + "recommend";
        
        // 先从缓存中获取
        if (redisService.hasKey(cacheKey)) {
            List<DishVO> cachedList = redisService.getList(cacheKey, DishVO.class);
            if (cachedList != null && !cachedList.isEmpty()) {
                return cachedList;
            }
        }
        
        // 缓存未命中，从数据库查询
        List<Dish> dishes = dishMapper.findByIsAvailableAndIsRecommendOrderBySortOrderAsc(1, 1);
        List<DishVO> result = convertToVOList(dishes);
        
        // 存入缓存
        redisService.setList(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        
        return result;
    }
    
    /**
     * 根据ID查询菜品（带缓存）
     */
    public DishVO getDishById(Long id) {
        String cacheKey = DISH_CACHE_KEY + id;
        
        // 先从缓存中获取
        if (redisService.hasKey(cacheKey)) {
            DishVO cachedDish = (DishVO) redisService.get(cacheKey);
            if (cachedDish != null) {
                return cachedDish;
            }
        }
        
        // 缓存未命中，从数据库查询
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }
        DishVO result = convertToVO(dish);
        
        // 存入缓存
        redisService.set(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        
        return result;
    }
    
    /**
     * 保存或更新菜品（清除缓存）
     */
    public DishVO saveDish(DishDTO dishDTO) {
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        if (dish.getId() == null) {
            dishMapper.insert(dish);
        } else {
            dishMapper.updateById(dish);
        }
        
        // 清除相关缓存
        clearDishCache();
        
        return convertToVO(dish);
    }
    
    /**
     * 删除菜品（清除缓存）
     */
    public void deleteDish(Long id) {
        dishMapper.deleteById(id);
        
        // 清除相关缓存
        clearDishCache();
        
        // 清除单个菜品缓存
        redisService.delete(DISH_CACHE_KEY + id);
    }
    
    /**
     * 清除菜品相关缓存
     */
    private void clearDishCache() {
        // 清除所有菜品列表缓存
        redisService.delete(DISH_LIST_CACHE_KEY + "available");
        redisService.delete(DISH_LIST_CACHE_KEY + "all");
        redisService.delete(DISH_LIST_CACHE_KEY + "recommend");
        
        // 注意：实际项目中应该使用模式匹配删除所有分类缓存
        // 这里简化处理，只删除已知的缓存键
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
