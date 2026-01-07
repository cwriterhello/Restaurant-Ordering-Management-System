package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.ComboDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ComboDishMapper extends BaseMapper<ComboDish> {
    
    @Select("SELECT * FROM combo_dishes WHERE combo_id = #{comboId}")
    List<ComboDish> selectByComboId(@Param("comboId") Long comboId);
    
    @Delete("DELETE FROM combo_dishes WHERE combo_id = #{comboId}")
    int deleteByComboId(@Param("comboId") Long comboId);
}