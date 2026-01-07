package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("stock_alerts")
@Data
public class StockAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("dish_id")
    private Long dishId;
    
    @TableField("dish_name")
    private String dishName;
    
    @TableField("current_stock")
    private Integer currentStock;
    
    @TableField("alert_threshold")
    private Integer alertThreshold = 10;
    
    @TableField("status")
    private String status = "ALERT"; // ALERT, RESOLVED
    
    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField("resolve_time")
    private LocalDateTime resolveTime;
}

