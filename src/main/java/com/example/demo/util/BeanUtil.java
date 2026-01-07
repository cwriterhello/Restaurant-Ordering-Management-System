package com.example.demo.util;

import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean 转换工具类
 */
public class BeanUtil {
    
    /**
     * 复制对象属性
     */
    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象转换失败", e);
        }
    }
    
    /**
     * 复制列表
     */
    public static <T, R> List<R> copyList(List<T> sourceList, Class<R> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return List.of();
        }
        return sourceList.stream()
                .map(item -> copyProperties(item, targetClass))
                .collect(Collectors.toList());
    }
}

