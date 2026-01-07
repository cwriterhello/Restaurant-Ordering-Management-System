package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Table;
import com.example.demo.mapper.TableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "*")
public class TableController {
    @Autowired
    private TableMapper tableMapper;
    
    @GetMapping
    public ApiResponse<List<Table>> getAllTables() {
        try {
            List<Table> tables = tableMapper.selectList(null);
            return ApiResponse.success(tables);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{tableNumber}")
    public ApiResponse<Table> getTableByNumber(@PathVariable String tableNumber) {
        try {
            Table table = tableMapper.findByTableNumber(tableNumber);
            if (table != null) {
                return ApiResponse.success(table);
            } else {
                return ApiResponse.error(404, "桌号不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{tableNumber}/validate")
    public ApiResponse<Boolean> validateTableNumber(@PathVariable String tableNumber) {
        try {
            Table table = tableMapper.findByTableNumber(tableNumber);
            boolean exists = table != null;
            return ApiResponse.success(exists);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

