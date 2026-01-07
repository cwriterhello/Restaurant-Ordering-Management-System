package com.example.demo.service;

import com.example.demo.entity.Category;
import com.example.demo.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    
    public List<Category> getActiveCategories() {
        return categoryMapper.findByStatusOrderBySortOrderAsc(1);
    }
    
    public Category getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        return category;
    }
    
    public Category saveCategory(Category category) {
        if (category.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
        return category;
    }
    
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }
}

