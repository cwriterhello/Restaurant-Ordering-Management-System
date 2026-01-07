package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
    @Select("SELECT * FROM dishes WHERE category_id = #{categoryId} AND is_available = #{isAvailable} ORDER BY sort_order ASC")
    List<Dish> findByCategoryIdAndIsAvailableOrderBySortOrderAsc(Long categoryId, Integer isAvailable);
    
    @Select("SELECT * FROM dishes WHERE is_available = #{isAvailable} AND is_recommend = #{isRecommend} ORDER BY sort_order ASC")
    List<Dish> findByIsAvailableAndIsRecommendOrderBySortOrderAsc(Integer isAvailable, Integer isRecommend);
    
    @Select("SELECT * FROM dishes WHERE is_available = #{isAvailable} ORDER BY sort_order ASC")
    List<Dish> findByIsAvailableOrderBySortOrderAsc(Integer isAvailable);
}

