package com.example.demo.service;

import com.example.demo.dto.ComboAndDishesRequest;
import com.example.demo.dto.ComboDishConfigDTO;
import com.example.demo.entity.Combo;
import com.example.demo.entity.ComboDish;
import com.example.demo.entity.Dish;
import com.example.demo.mapper.ComboDishMapper;
import com.example.demo.mapper.ComboMapper;
import com.example.demo.mapper.DishMapper;
import com.example.demo.vo.ComboVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

        List<ComboDish> relations = comboDishMapper.selectByComboId(id);
        Set<Long> dishIds = relations.stream()
                .map(ComboDish::getDishId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Dish> dishMap = dishIds.isEmpty()
                ? Collections.emptyMap()
                : dishMapper.selectBatchIds(dishIds).stream()
                .collect(Collectors.toMap(Dish::getId, dish -> dish, (left, right) -> left));

        List<ComboVO.ComboDishVO> comboDishes = relations.stream()
                .map(relation -> {
                    Dish dish = dishMap.get(relation.getDishId());
                    if (dish == null) {
                        return null;
                    }
                    ComboVO.ComboDishVO vo = new ComboVO.ComboDishVO();
                    vo.setDishId(relation.getDishId());
                    vo.setDishName(dish.getName());
                    vo.setPrice(dish.getPrice());
                    vo.setQuantity(relation.getQuantity());
                    return vo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        comboVO.setDishes(comboDishes);

        return comboVO;
    }

    @Transactional
    public void createComboWithRelation(ComboAndDishesRequest request) {
        Combo combo = request.getCombo();
        ComboDishConfigDTO dishesConfig = request.getDishesConfig();

        comboMapper.insert(combo);

        if (dishesConfig != null && dishesConfig.getDishes() != null && !dishesConfig.getDishes().isEmpty()) {
            for (ComboDishConfigDTO.ComboDishItemDTO item : dishesConfig.getDishes()) {
                ComboDish comboDish = new ComboDish();
                comboDish.setComboId(combo.getId());
                comboDish.setDishId(item.getDishId());
                comboDish.setQuantity(item.getQuantity() != null ? item.getQuantity() : 1);
                comboDishMapper.insert(comboDish);
            }
        }
    }

    public Combo getComboById(Long id) {
        Combo combo = comboMapper.selectById(id);
        if (combo == null) {
            throw new RuntimeException("套餐不存在");
        }
        return combo;
    }

    public Combo saveCombo(Combo combo) {
        if (combo.getName() == null || combo.getName().trim().isEmpty()) {
            throw new RuntimeException("套餐名称不能为空");
        }
        if (combo.getPrice() == null) {
            throw new RuntimeException("套餐价格不能为空");
        }

        if (combo.getId() == null || combo.getId() == 0) {
            comboMapper.insert(combo);
        } else {
            comboMapper.updateById(combo);
        }
        return combo;
    }

    public void configureComboDishes(ComboDishConfigDTO config) {
        Long comboId = config.getComboId();

        comboDishMapper.deleteByComboId(comboId);

        if (config.getDishes() != null && !config.getDishes().isEmpty()) {
            for (ComboDishConfigDTO.ComboDishItemDTO item : config.getDishes()) {
                ComboDish comboDish = new ComboDish();
                comboDish.setComboId(comboId);
                comboDish.setDishId(item.getDishId());
                comboDish.setQuantity(item.getQuantity() != null ? item.getQuantity() : 1);
                comboDishMapper.insert(comboDish);
            }
        }
    }
}
