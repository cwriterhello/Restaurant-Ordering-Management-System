package com.example.demo.service;

import com.example.demo.dto.ComboDishConfigDTO;
import com.example.demo.entity.Combo;
import com.example.demo.entity.ComboDish;
import com.example.demo.mapper.ComboMapper;
import com.example.demo.mapper.ComboDishMapper;
import com.example.demo.mapper.DishMapper;
import com.example.demo.vo.ComboVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComboService {
    @Autowired
    private ComboMapper comboMapper;
    
    @Autowired
    private ComboDishMapper comboDishMapper;
    
    @Autowired
    private DishMapper dishMapper;
    
    public List<Combo> getAvailableCombos() {
        return comboMapper.findByIsAvailableOrderBySortOrderAsc(1);
    }
    
    public Combo getComboById(Long id) {
        Combo combo = comboMapper.selectById(id);
        if (combo == null) {
            throw new RuntimeException("套餐不存在");
        }
        return combo;
    }
    
    public Combo saveCombo(Combo combo) {
        if (combo.getId() == null) {
            comboMapper.insert(combo);
        } else {
            comboMapper.updateById(combo);
        }
        return combo;
    }
    
    public void deleteCombo(Long id) {
        comboMapper.deleteById(id);
        // 删除相关的套餐菜品关联
        comboDishMapper.deleteByComboId(id);
    }
    
    @Transactional
    public void configureComboDishes(ComboDishConfigDTO config) {
        // 先删除现有的套餐菜品关联
        comboDishMapper.deleteByComboId(config.getComboId());
        
        // 添加新的套餐菜品关联
        for (ComboDishConfigDTO.ComboDishItemDTO item : config.getDishes()) {
            ComboDish comboDish = new ComboDish();
            comboDish.setComboId(config.getComboId());
            comboDish.setDishId(item.getDishId());
            comboDish.setQuantity(item.getQuantity() != null ? item.getQuantity() : 1);
            comboDishMapper.insert(comboDish);
        }
    }
    
    public ComboVO getComboWithDishes(Long id) {
        Combo combo = getComboById(id);
        ComboVO comboVO = new ComboVO();
        comboVO.setId(combo.getId());
        comboVO.setName(combo.getName());
        comboVO.setDescription(combo.getDescription());
        comboVO.setImage(combo.getImage());
        comboVO.setPrice(combo.getPrice());
        comboVO.setOriginalPrice(combo.getOriginalPrice());
        comboVO.setIsAvailable(combo.getIsAvailable());
        comboVO.setSortOrder(combo.getSortOrder());
        comboVO.setCreateTime(combo.getCreateTime());
        comboVO.setUpdateTime(combo.getUpdateTime());
        
        // 获取套餐关联的菜品
        List<ComboVO.ComboDishVO> comboDishes = comboDishMapper.selectByComboId(id).stream()
                .map(cd -> {
                    ComboVO.ComboDishVO vo = new ComboVO.ComboDishVO();
                    vo.setDishId(cd.getDishId());
                    vo.setDishName(dishMapper.selectById(cd.getDishId()).getName());
                    vo.setQuantity(cd.getQuantity());
                    return vo;
                })
                .collect(Collectors.toList());
        comboVO.setDishes(comboDishes);
        
        return comboVO;
    }
}