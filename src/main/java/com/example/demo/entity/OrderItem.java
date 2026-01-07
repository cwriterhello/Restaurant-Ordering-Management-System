package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("order_items")
@Data
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("order_id")
    private Long orderId;
    
    @TableField("item_type")
    private String itemType; // DISH, COMBO
    
    @TableField("item_id")
    private Long itemId;
    
    @TableField("item_name")
    private String itemName;
    
    @TableField("price")
    private BigDecimal price;
    
    @TableField("quantity")
    private Integer quantity;
    
    @TableField("subtotal")
    private BigDecimal subtotal;
    
    @TableField("status")
    private String status = "PENDING"; // PENDING, PREPARING, READY
    
    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
}

