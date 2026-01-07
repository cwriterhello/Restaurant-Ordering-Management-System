package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    @Select("SELECT * FROM orders WHERE order_number = #{orderNumber}")
    Order findByOrderNumber(String orderNumber);
    
    @Select("<script>SELECT * FROM orders WHERE table_id = #{tableId} AND status IN " +
            "<foreach collection='statuses' item='status' open='(' separator=',' close=')'>" +
            "#{status}" +
            "</foreach></script>")
    List<Order> findByTableIdAndStatusIn(Long tableId, List<String> statuses);
    
    @Select("SELECT * FROM orders WHERE status = #{status} ORDER BY create_time ASC")
    List<Order> findByStatusOrderByCreateTimeAsc(String status);
    
    @Select("SELECT * FROM orders WHERE create_time BETWEEN #{start} AND #{end}")
    List<Order> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Select("SELECT * FROM orders WHERE status IN ('PENDING', 'CONFIRMED', 'PREPARING') ORDER BY create_time ASC")
    List<Order> findActiveOrders();
}

