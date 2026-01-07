package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("orders")
@Data
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("order_number")
    private String orderNumber;
    
    @TableField("table_id")
    private Long tableId;
    
    @TableField("table_number")
    private String tableNumber;
    
    @TableField("member_id")
    private Long memberId;
    
    @TableField("member_phone")
    private String memberPhone;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @TableField("actual_amount")
    private BigDecimal actualAmount;
    
    @TableField("status")
    private String status = "PENDING"; // PENDING, CONFIRMED, PREPARING, READY, PAID, CANCELLED
    
    @TableField("payment_method")
    private String paymentMethod; // CASH, ALIPAY, WECHAT, MEMBER
    
    @TableField("payment_time")
    private LocalDateTime paymentTime;
    
    @TableField("cashier_id")
    private Long cashierId;
    
    @TableField("remark")
    private String remark;
    
    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

