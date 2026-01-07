package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotBlank(message = "桌号不能为空")
    private String tableNumber;
    
    private String memberPhone; // 可选，会员手机号
    
    private String remark; // 备注
    
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemDTO> items;
    
    @Data
    public static class OrderItemDTO {
        @NotBlank(message = "商品类型不能为空")
        private String itemType; // DISH 或 COMBO
        
        @NotBlank(message = "商品ID不能为空")
        private Long itemId;
        
        @NotBlank(message = "数量不能为空")
        private Integer quantity;
    }
}

