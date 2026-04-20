package com.example.demo.service;

import com.example.demo.dto.ComboAndDishesRequest;
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

/**
 * 套餐业务服务类
 * 提供套餐的增删改查及套餐与菜品关联关系的管理功能
 */
@Service
public class ComboService {
    
    /**
     * 套餐数据访问对象
     */
    @Autowired
    private ComboMapper comboMapper;
    
    /**
     * 套餐菜品关联数据访问对象
     */
    @Autowired
    private ComboDishMapper comboDishMapper;
    
    /**
     * 菜品数据访问对象
     */
    @Autowired
    private DishMapper dishMapper;

    /**
     * 查询所有可用的套餐
     * @return 可用套餐列表，按排序字段升序排列
     */
    public List<Combo> getAvailableCombos() {
        return comboMapper.findByIsAvailableOrderBySortOrderAsc(1);
    }

    /**
     * 根据套餐ID查询对应的菜品
     * @param id 套餐ID
     * @return 包含菜品信息的完整套餐详情
     * @throws RuntimeException 当套餐不存在时抛出异常
     */
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
                    var dish = dishMapper.selectById(cd.getDishId());
                    vo.setDishName(dish.getName());
                    vo.setPrice(dish.getPrice());
                    vo.setQuantity(cd.getQuantity());
                    return vo;
                })
                .collect(Collectors.toList());
        comboVO.setDishes(comboDishes);
        
        return comboVO;
    }

    /**
     * 创建套餐和菜品之间的关联关系
     * 使用事务确保数据一致性
     * @param request 包含套餐信息和菜品配置的请求对象
     * @throws RuntimeException 当数据库操作失败时抛出异常
     */
    @Transactional
    public void createComboWithRelation(ComboAndDishesRequest request) {
        Combo combo = request.getCombo();
        ComboDishConfigDTO dishesConfig = request.getDishesConfig();
        
        // 先保存套餐
        comboMapper.insert(combo);
        
        // 再配置菜品关联关系
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

    /**
     * 根据ID获取套餐
     * @param id 套餐ID
     * @return 套餐实体
     * @throws RuntimeException 当套餐不存在时抛出异常
     */
    public Combo getComboById(Long id) {
        Combo combo = comboMapper.selectById(id);
        if (combo == null) {
            throw new RuntimeException("套餐不存在");
        }
        return combo;
    }

    /**
     * 保存套餐
     * @param combo 套餐实体
     * @return 保存后的套餐
     */
    /**
     * 保存套餐
     * @param combo 套餐实体
     * @return 保存后的套餐
     */
    public Combo saveCombo(Combo combo) {
        // 确保必填字段不为空
        if (combo.getName() == null || combo.getName().trim().isEmpty()) {
            throw new RuntimeException("套餐名称不能为空");
        }
        if (combo.getPrice() == null) {
            throw new RuntimeException("套餐价格不能为空");
        }
        
        if (combo.getId() == 0) {
            // 新增套餐
            comboMapper.insert(combo);
        } else {
            // 更新套餐
            comboMapper.updateById(combo);
        }
        return combo;
    }

    /**
     * 配置套餐和菜品的关联关系
     * @param config 菜品配置信息
     */

    public void configureComboDishes(ComboDishConfigDTO config) {
        Long comboId = config.getComboId();
        
        // 先删除原有的关联关系
        comboDishMapper.deleteByComboId(comboId);
        
        // 再插入新的关联关系
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