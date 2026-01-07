package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class ComboDishConfigDTO {
    private Long comboId;
    private List<ComboDishItemDTO> dishes;
    
    @Data
    public static class ComboDishItemDTO {
        private Long dishId;
        private Integer quantity; // 数量，默认为1
    }
}