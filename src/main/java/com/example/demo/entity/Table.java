package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("tables")
@Data
public class Table {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("table_number")
    private String tableNumber;
    
    @TableField("qr_code")
    private String qrCode;
    
    @TableField("capacity")
    private Integer capacity = 4;
    
    @TableField("status")
    private String status = "EMPTY"; // EMPTY, OCCUPIED, RESERVED
    
    @TableField("current_order_id")
    private Long currentOrderId;
    
    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

