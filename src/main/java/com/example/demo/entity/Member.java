package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("members")
@Data
public class Member {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("phone")
    private String phone;
    
    @TableField("name")
    private String name;
    
    @TableField("level")
    private String level = "NORMAL"; // NORMAL, SILVER, GOLD, PLATINUM
    
    @TableField("discount")
    private BigDecimal discount = new BigDecimal("1.00");
    
    @TableField("points")
    private Integer points = 0;
    
    @TableField("total_consumption")
    private BigDecimal totalConsumption = BigDecimal.ZERO;
    
    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

