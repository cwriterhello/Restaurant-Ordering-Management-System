package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.DailyStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyStatisticsMapper extends BaseMapper<DailyStatistics> {
    @Select("SELECT * FROM daily_statistics WHERE stat_date = #{statDate}")
    DailyStatistics findByStatDate(LocalDate statDate);
    
    @Select("SELECT * FROM daily_statistics WHERE stat_date BETWEEN #{start} AND #{end} ORDER BY stat_date DESC")
    List<DailyStatistics> findByStatDateBetweenOrderByStatDateDesc(LocalDate start, LocalDate end);
}

