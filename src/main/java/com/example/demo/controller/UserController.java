package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ApiResponse<List<UserVO>> getAllUsers() {
        try {
            List<UserVO> users = userService.getAllUsers();
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUserById(@PathVariable Long id) {
        try {
            UserVO user = userService.getUserById(id);
            if (user != null) {
                return ApiResponse.success(user);
            } else {
                return ApiResponse.error(404, "用户不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User user) {
        try {
            // 验证角色权限：管理员只能创建收银员和后厨
            if ("ADMIN".equals(user.getRole()) && !user.getUsername().equals("admin")) {
                return ApiResponse.error("权限不足，无法创建管理员账号");
            }
            User savedUser = userService.saveUser(user);
            return ApiResponse.success("用户创建成功", savedUser);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            // 验证角色权限：不能修改管理员账号
            if (user.getId().equals(1L) && !"admin".equals(user.getUsername())) {
                return ApiResponse.error("权限不足，无法修改管理员账号");
            }
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            return ApiResponse.success("用户更新成功", updatedUser);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        try {
            // 验证角色权限：不能删除管理员账号
            if (id.equals(1L)) {
                return ApiResponse.error("权限不足，无法删除管理员账号");
            }
            userService.deleteUser(id);
            return ApiResponse.success("用户删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            // 验证角色权限：不能重置管理员密码
            if (id.equals(1L)) {
                return ApiResponse.error("权限不足，无法重置管理员密码");
            }
            String newPassword = body.get("newPassword");
            if (newPassword == null || newPassword.isEmpty()) {
                return ApiResponse.error("新密码不能为空");
            }
            userService.resetPassword(id, newPassword);
            return ApiResponse.success("密码重置成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}