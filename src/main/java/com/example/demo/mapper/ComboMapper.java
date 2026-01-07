package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Combo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ComboMapper extends BaseMapper<Combo> {
    @Select("SELECT * FROM combos WHERE is_available = #{isAvailable} ORDER BY sort_order ASC")
    List<Combo> findByIsAvailableOrderBySortOrderAsc(Integer isAvailable);
}

