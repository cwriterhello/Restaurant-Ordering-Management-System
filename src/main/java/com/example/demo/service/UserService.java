package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<UserVO> getAllUsers() {
        return userMapper.selectList(null).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        return convertToVO(user);
    }
    
    public User saveUser(User user) {
        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.insert(user);
        return user;
    }
    
    public User updateUser(User user) {
        // 如果提供了新密码，则加密
        User existingUser = userMapper.selectById(user.getId());
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // 保持原密码
            user.setPassword(existingUser.getPassword());
        } else {
            // 加密新密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.updateById(user);
        return user;
    }
    
    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userMapper.updateById(user);
        }
    }
    
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
    
    private UserVO convertToVO(User user) {
        if (user == null) {return null;}
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}