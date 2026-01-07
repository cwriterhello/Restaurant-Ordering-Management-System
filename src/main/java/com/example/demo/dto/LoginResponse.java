package com.example.demo.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private String realName;
    private Long userId; // 添加用户ID字段
    
    public LoginResponse(String token, String username, String role, String realName, Long userId) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.realName = realName;
        this.userId = userId;
    }
}