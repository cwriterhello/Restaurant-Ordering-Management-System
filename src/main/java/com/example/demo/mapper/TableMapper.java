package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Table;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TableMapper extends BaseMapper<Table> {
    @Select("SELECT * FROM tables WHERE table_number = #{tableNumber}")
    Table findByTableNumber(String tableNumber);
}

