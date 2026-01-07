package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("daily_statistics")
@Data
public class DailyStatistics {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("stat_date")
    private LocalDate statDate;
    
    @TableField("total_orders")
    private Integer totalOrders = 0;
    
    @TableField("total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @TableField("total_discount")
    private BigDecimal totalDiscount = BigDecimal.ZERO;
    
    @TableField("cash_amount")
    private BigDecimal cashAmount = BigDecimal.ZERO;
    
    @TableField("alipay_amount")
    private BigDecimal alipayAmount = BigDecimal.ZERO;
    
    @TableField("wechat_amount")
    private BigDecimal wechatAmount = BigDecimal.ZERO;
    
    @TableField("member_amount")
    private BigDecimal memberAmount = BigDecimal.ZERO;
    
    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

