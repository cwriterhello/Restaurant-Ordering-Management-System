package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderStatusUpdateDTO;
import com.example.demo.dto.PaymentDTO;
import com.example.demo.service.OrderService;
import com.example.demo.vo.OrderVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    @Autowired
    private OrderService orderService;
    
    @PostMapping
    public ApiResponse<OrderVO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            OrderVO order = orderService.createOrder(request);
            return ApiResponse.success("订单创建成功", order);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/active")
    public ApiResponse<List<OrderVO>> getActiveOrders() {
        try {
            List<OrderVO> orders = orderService.getActiveOrders();
            return ApiResponse.success(orders);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping
    public ApiResponse<List<OrderVO>> getAllOrders() {
        try {
            List<OrderVO> orders = orderService.getAllOrders();
            return ApiResponse.success(orders);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/ready")
    public ApiResponse<List<OrderVO>> getReadyOrders() {
        try {
            List<OrderVO> orders = orderService.getReadyOrders();
            return ApiResponse.success(orders);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/status")
    public ApiResponse<OrderVO> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateDTO dto) {
        try {
            OrderVO order = orderService.updateOrderStatus(id, dto);
            return ApiResponse.success("订单状态更新成功", order);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/pay")
    public ApiResponse<OrderVO> payOrder(
            @PathVariable Long id,
            @Valid @RequestBody PaymentDTO paymentDTO) {
        try {
            OrderVO order = orderService.payOrder(id, paymentDTO);
            return ApiResponse.success("支付成功", order);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}