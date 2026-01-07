package com.example.demo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNumber;
    private Long tableId;
    private String tableNumber;
    private Long memberId;
    private String memberPhone;
    private String memberName;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal actualAmount;
    private String status; // PENDING, CONFIRMED, PREPARING, READY, PAID, CANCELLED
    private String paymentMethod; // CASH, ALIPAY, WECHAT, MEMBER
    private LocalDateTime paymentTime;
    private Long cashierId;
    private String cashierName;
    private String remark;
    private List<OrderItemVO> items;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

