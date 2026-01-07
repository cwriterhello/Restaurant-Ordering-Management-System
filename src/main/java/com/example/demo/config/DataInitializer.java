package com.example.demo.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 初始化管理员账户（如果不存在）
        LambdaQueryWrapper<User> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(User::getUsername, "admin");
        if (userMapper.selectCount(adminWrapper) == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setRole("ADMIN");
            admin.setPhone("13800138000");
            admin.setStatus(1);
            userMapper.insert(admin);
            System.out.println("管理员账户初始化完成：admin / admin123");
        }
        
        // 初始化收银员账户
        LambdaQueryWrapper<User> cashierWrapper = new LambdaQueryWrapper<>();
        cashierWrapper.eq(User::getUsername, "cashier1");
        if (userMapper.selectCount(cashierWrapper) == 0) {
            User cashier = new User();
            cashier.setUsername("cashier1");
            cashier.setPassword(passwordEncoder.encode("admin123"));
            cashier.setRealName("收银员1");
            cashier.setRole("CASHIER");
            cashier.setPhone("13800138001");
            cashier.setStatus(1);
            userMapper.insert(cashier);
            System.out.println("收银员账户初始化完成：cashier1 / admin123");
        }
        
        // 初始化后厨账户
        LambdaQueryWrapper<User> kitchenWrapper = new LambdaQueryWrapper<>();
        kitchenWrapper.eq(User::getUsername, "kitchen1");
        if (userMapper.selectCount(kitchenWrapper) == 0) {
            User kitchen = new User();
            kitchen.setUsername("kitchen1");
            kitchen.setPassword(passwordEncoder.encode("admin123"));
            kitchen.setRealName("后厨1");
            kitchen.setRole("KITCHEN");
            kitchen.setPhone("13800138002");
            kitchen.setStatus(1);
            userMapper.insert(kitchen);
            System.out.println("后厨账户初始化完成：kitchen1 / admin123");
        }
    }
}