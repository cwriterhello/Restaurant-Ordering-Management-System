package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.StockAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface StockAlertMapper extends BaseMapper<StockAlert> {
    @Select("SELECT * FROM stock_alerts WHERE status = #{status} ORDER BY create_time DESC")
    List<StockAlert> findByStatusOrderByCreateTimeDesc(String status);
    
    @Select("SELECT * FROM stock_alerts WHERE dish_id = #{dishId} AND status = #{status}")
    List<StockAlert> findByDishIdAndStatus(Long dishId, String status);
}

