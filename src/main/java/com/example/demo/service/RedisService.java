package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务
 * 提供通用的缓存操作方法
 */
@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存并指定过期时间
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     * @param key 缓存键
     * @return 缓存值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     * @param key 缓存键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 判断key是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置列表缓存
     * @param key 缓存键
     * @param list 缓存列表
     */
    public void setList(String key, List<?> list) {
        redisTemplate.opsForValue().set(key, list);
    }

    /**
     * 设置列表缓存并指定过期时间
     * @param key 缓存键
     * @param list 缓存列表
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void setList(String key, List<?> list, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, list, timeout, unit);
    }

    /**
     * 获取列表缓存
     * @param key 缓存键
     * @return 缓存列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key, Class<T> clazz) {
        return (List<T>) redisTemplate.opsForValue().get(key);
    }
}
