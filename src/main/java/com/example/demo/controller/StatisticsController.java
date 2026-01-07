package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.DailyStatistics;
import com.example.demo.entity.StockAlert;
import com.example.demo.mapper.DailyStatisticsMapper;
import com.example.demo.mapper.StockAlertMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {
    @Autowired
    private DailyStatisticsMapper dailyStatisticsMapper;
    
    @Autowired
    private StockAlertMapper stockAlertMapper;
    
    @GetMapping("/daily")
    public ApiResponse<List<DailyStatistics>> getDailyStatistics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        try {
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(7);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            List<DailyStatistics> statistics = dailyStatisticsMapper
                    .findByStatDateBetweenOrderByStatDateDesc(startDate, endDate);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/stock-alerts")
    public ApiResponse<List<StockAlert>> getStockAlerts() {
        try {
            List<StockAlert> alerts = stockAlertMapper.findByStatusOrderByCreateTimeDesc("ALERT");
            return ApiResponse.success(alerts);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

