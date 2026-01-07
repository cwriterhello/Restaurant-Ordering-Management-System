package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("combo_dishes")
@Data
public class ComboDish {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("combo_id")
    private Long comboId;
    
    @TableField("dish_id")
    private Long dishId;
    
    @TableField("quantity")
    private Integer quantity = 1; // 数量，默认为1
    
    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
}