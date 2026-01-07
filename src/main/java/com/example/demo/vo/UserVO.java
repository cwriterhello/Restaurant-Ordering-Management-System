package com.example.demo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String role; // ADMIN, CASHIER, KITCHEN
    private String phone;
    private Integer status; // 1-启用，0-禁用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

