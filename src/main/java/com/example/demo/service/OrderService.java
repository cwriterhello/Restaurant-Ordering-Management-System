package com.example.demo.service;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderStatusUpdateDTO;
import com.example.demo.dto.PaymentDTO;
import com.example.demo.entity.*;
import com.example.demo.mapper.*;
import com.example.demo.util.BeanUtil;
import com.example.demo.vo.OrderItemVO;
import com.example.demo.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private TableMapper tableMapper;
    
    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private ComboMapper comboMapper;
    
    @Autowired
    private MemberMapper memberMapper;
    
    @Autowired
    private StockAlertMapper stockAlertMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private DailyStatisticsMapper dailyStatisticsMapper;
    
    @Transactional
    public OrderVO createOrder(CreateOrderRequest request) {
        // 获取桌号信息
        Table table = tableMapper.findByTableNumber(request.getTableNumber());
        if (table == null) {
            throw new RuntimeException("桌号不存在");
        }
        
        if (!"EMPTY".equals(table.getStatus()) && !"OCCUPIED".equals(table.getStatus())) {
            throw new RuntimeException("该桌号当前不可用");
        }
        
        // 生成订单号
        String orderNumber = generateOrderNumber();
        
        // 计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        // 处理会员折扣
        BigDecimal discount = BigDecimal.ONE;
        Member member = null;
        if (request.getMemberPhone() != null && !request.getMemberPhone().isEmpty()) {
            member = memberMapper.findByPhone(request.getMemberPhone());
            if (member != null) {
                discount = member.getDiscount();
            }
        }
        
        // 处理订单项
        for (CreateOrderRequest.OrderItemDTO itemDTO : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(null); // 先设为null，保存订单后再设置
            orderItem.setItemType(itemDTO.getItemType());
            orderItem.setItemId(itemDTO.getItemId());
            orderItem.setQuantity(itemDTO.getQuantity());
            
            BigDecimal itemPrice;
            String itemName;
            
            if ("DISH".equals(itemDTO.getItemType())) {
                Dish dish = dishMapper.selectById(itemDTO.getItemId());
                if (dish == null) {
                    throw new RuntimeException("菜品不存在");
                }
                
                if (dish.getIsAvailable() == 0) {
                    throw new RuntimeException("菜品" + dish.getName() + "已下架");
                }
                
                if (dish.getStock() < itemDTO.getQuantity()) {
                    throw new RuntimeException("菜品" + dish.getName() + "库存不足");
                }
                
                itemPrice = dish.getPrice();
                itemName = dish.getName();
                
                // 更新库存
                dish.setStock(dish.getStock() - itemDTO.getQuantity());
                dishMapper.updateById(dish);
                
                // 检查库存预警
                checkStockAlert(dish);
                
            } else if ("COMBO".equals(itemDTO.getItemType())) {
                Combo combo = comboMapper.selectById(itemDTO.getItemId());
                if (combo == null) {
                    throw new RuntimeException("套餐不存在");
                }
                
                if (combo.getIsAvailable() == 0) {
                    throw new RuntimeException("套餐" + combo.getName() + "已下架");
                }
                
                itemPrice = combo.getPrice();
                itemName = combo.getName();
            } else {
                throw new RuntimeException("无效的商品类型");
            }
            
            BigDecimal subtotal = itemPrice.multiply(new BigDecimal(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            
            orderItem.setItemName(itemName);
            orderItem.setPrice(itemPrice);
            orderItem.setSubtotal(subtotal);
            orderItem.setStatus("PENDING");
            
            orderItems.add(orderItem);
        }
        
        // 计算折扣金额
        BigDecimal discountAmount = totalAmount.multiply(BigDecimal.ONE.subtract(discount));
        BigDecimal actualAmount = totalAmount.subtract(discountAmount);
        
        // 创建订单
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setTableId(table.getId());
        order.setTableNumber(table.getTableNumber());
        if (member != null) {
            order.setMemberId(member.getId());
            order.setMemberPhone(member.getPhone());
        }
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setActualAmount(actualAmount);
        order.setStatus("PENDING");
        order.setRemark(request.getRemark());
        
        orderMapper.insert(order);
        
        // 保存订单项
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        
        // 更新桌号状态
        table.setStatus("OCCUPIED");
        table.setCurrentOrderId(order.getId());
        tableMapper.updateById(table);
        
        // 通过WebSocket通知后厨
        webSocketService.sendOrderToKitchen(order);
        
        return convertToVO(order);
    }
    
    private String generateOrderNumber() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
               String.format("%04d", (int)(Math.random() * 10000));
    }
    
    private void checkStockAlert(Dish dish) {
        if (dish.getStock() <= 10) {
            // 检查是否已有预警
            List<StockAlert> existingAlerts = stockAlertMapper.findByDishIdAndStatus(dish.getId(), "ALERT");
            if (existingAlerts == null || existingAlerts.isEmpty()) {
                StockAlert alert = new StockAlert();
                alert.setDishId(dish.getId());
                alert.setDishName(dish.getName());
                alert.setCurrentStock(dish.getStock());
                alert.setAlertThreshold(10);
                alert.setStatus("ALERT");
                stockAlertMapper.insert(alert);

                // 通过 WebSocket 实时推送库存预警给管理端
                webSocketService.sendStockAlert(
                        "【库存预警】" + dish.getName() + " 当前库存为 " + dish.getStock() + "，请及时补货。"
                );
            }
        }
    }
    
    public List<OrderVO> getActiveOrders() {
        List<Order> orders = orderMapper.findActiveOrders();
        return orders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    public List<OrderVO> getAllOrders() {
        List<Order> orders = orderMapper.selectList(null); // 获取所有订单
        return orders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    public List<OrderVO> getReadyOrders() {
        List<Order> orders = orderMapper.findByStatusOrderByCreateTimeAsc("READY"); // 获取状态为READY的订单
        return orders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    public OrderVO updateOrderStatus(Long orderId, OrderStatusUpdateDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setStatus(dto.getStatus());
        orderMapper.updateById(order);
        
        // 通知前端订单状态更新
        webSocketService.sendOrderStatusUpdate(order);
        
        return convertToVO(order);
    }
    
    @Transactional
    public OrderVO payOrder(Long orderId, PaymentDTO paymentDTO) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!"READY".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法支付");
        }
        
        order.setStatus("PAID");
        order.setPaymentMethod(paymentDTO.getPaymentMethod());
        order.setPaymentTime(LocalDateTime.now());
        order.setCashierId(paymentDTO.getCashierId());
        orderMapper.updateById(order);
        
        // 更新桌号状态
        Table table = tableMapper.selectById(order.getTableId());
        if (table == null) {
            throw new RuntimeException("桌号不存在");
        }
        table.setStatus("EMPTY");
        table.setCurrentOrderId(null);
        tableMapper.updateById(table);
        
        // 更新会员消费记录
        if (order.getMemberId() != null) {
            Member member = memberMapper.selectById(order.getMemberId());
            if (member == null) {
                throw new RuntimeException("会员不存在");
            }
            member.setTotalConsumption(member.getTotalConsumption().add(order.getActualAmount()));
            // 每消费1元获得1积分
            member.setPoints(member.getPoints() + order.getActualAmount().intValue());
            memberMapper.updateById(member);
        }
        
        // 更新统计数据
        updateDailyStatistics(order);
        
        return convertToVO(order);
    }
    
    private void updateDailyStatistics(Order order) {
        LocalDate statDate = order.getPaymentTime().toLocalDate();
        DailyStatistics stats = dailyStatisticsMapper.findByStatDate(statDate);
        if (stats == null) {
            stats = new DailyStatistics();
            stats.setStatDate(statDate);
        }

        // 订单数 +1
        stats.setTotalOrders(stats.getTotalOrders() + 1);

        // 累加金额与折扣
        stats.setTotalAmount(stats.getTotalAmount().add(order.getTotalAmount()));
        stats.setTotalDiscount(stats.getTotalDiscount().add(order.getDiscountAmount()));

        // 按支付方式统计
        switch (order.getPaymentMethod()) {
            case "CASH" -> stats.setCashAmount(
                    stats.getCashAmount().add(order.getActualAmount())
            );
            case "ALIPAY" -> stats.setAlipayAmount(
                    stats.getAlipayAmount().add(order.getActualAmount())
            );
            case "WECHAT" -> stats.setWechatAmount(
                    stats.getWechatAmount().add(order.getActualAmount())
            );
            default -> {
                // 其它方式暂不区分
            }
        }

        // 会员支付额
        if (order.getMemberId() != null) {
            stats.setMemberAmount(
                    stats.getMemberAmount().add(order.getActualAmount())
            );
        }

        if (stats.getId() == null) {
            dailyStatisticsMapper.insert(stats);
        } else {
            dailyStatisticsMapper.updateById(stats);
        }
    }
    
    private OrderVO convertToVO(Order order) {
        OrderVO vo = BeanUtil.copyProperties(order, OrderVO.class);
        
        // 查询订单项
        List<OrderItem> items = orderItemMapper.findByOrderId(order.getId());
        List<OrderItemVO> itemVOs = items.stream()
                .map(item -> BeanUtil.copyProperties(item, OrderItemVO.class))
                .collect(Collectors.toList());
        vo.setItems(itemVOs);
        
        // 查询会员信息
        if (order.getMemberId() != null) {
            Member member = memberMapper.selectById(order.getMemberId());
            if (member != null) {
                vo.setMemberName(member.getName());
            }
        }
        
        // 查询收银员信息
        if (order.getCashierId() != null) {
            User cashier = userMapper.selectById(order.getCashierId());
            if (cashier != null) {
                vo.setCashierName(cashier.getRealName());
            }
        }
        
        return vo;
    }
}

