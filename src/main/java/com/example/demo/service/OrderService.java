package com.example.demo.service;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderStatusUpdateDTO;
import com.example.demo.dto.PaymentDTO;
import com.example.demo.entity.Combo;
import com.example.demo.entity.DailyStatistics;
import com.example.demo.entity.Dish;
import com.example.demo.entity.Member;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.StockAlert;
import com.example.demo.entity.Table;
import com.example.demo.entity.User;
import com.example.demo.mapper.ComboMapper;
import com.example.demo.mapper.DailyStatisticsMapper;
import com.example.demo.mapper.DishMapper;
import com.example.demo.mapper.MemberMapper;
import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.StockAlertMapper;
import com.example.demo.mapper.TableMapper;
import com.example.demo.mapper.UserMapper;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
        Table table = tableMapper.findByTableNumber(request.getTableNumber());
        if (table == null) {
            throw new RuntimeException("桌号不存在");
        }

        if (!"EMPTY".equals(table.getStatus()) && !"OCCUPIED".equals(table.getStatus())) {
            throw new RuntimeException("该桌号当前不可用");
        }

        String orderNumber = generateOrderNumber();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal discount = BigDecimal.ONE;
        Member member = null;
        if (request.getMemberPhone() != null && !request.getMemberPhone().isEmpty()) {
            member = memberMapper.findByPhone(request.getMemberPhone());
            if (member != null) {
                discount = member.getDiscount();
            }
        }

        for (CreateOrderRequest.OrderItemDTO itemDTO : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(null);
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

                dish.setStock(dish.getStock() - itemDTO.getQuantity());
                dishMapper.updateById(dish);
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

        BigDecimal discountAmount = totalAmount.multiply(BigDecimal.ONE.subtract(discount));
        BigDecimal actualAmount = totalAmount.subtract(discountAmount);

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

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        table.setStatus("OCCUPIED");
        table.setCurrentOrderId(order.getId());
        tableMapper.updateById(table);

        webSocketService.sendOrderToKitchen(order);
        return convertToVO(order);
    }

    private String generateOrderNumber() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    private void checkStockAlert(Dish dish) {
        if (dish.getStock() <= 10) {
            List<StockAlert> existingAlerts = stockAlertMapper.findByDishIdAndStatus(dish.getId(), "ALERT");
            if (existingAlerts == null || existingAlerts.isEmpty()) {
                StockAlert alert = new StockAlert();
                alert.setDishId(dish.getId());
                alert.setDishName(dish.getName());
                alert.setCurrentStock(dish.getStock());
                alert.setAlertThreshold(10);
                alert.setStatus("ALERT");
                stockAlertMapper.insert(alert);

                webSocketService.sendStockAlert(
                        "【库存预警】" + dish.getName() + " 当前库存仅" + dish.getStock() + "，请及时补货。"
                );
            }
        }
    }

    public List<OrderVO> getActiveOrders() {
        return convertToVOList(orderMapper.findActiveOrders());
    }

    public List<OrderVO> getAllOrders() {
        return convertToVOList(orderMapper.selectList(null));
    }

    public List<OrderVO> getReadyOrders() {
        return convertToVOList(orderMapper.findByStatusOrderByCreateTimeAsc("READY"));
    }

    public OrderVO updateOrderStatus(Long orderId, OrderStatusUpdateDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setStatus(dto.getStatus());
        orderMapper.updateById(order);

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

        Table table = tableMapper.selectById(order.getTableId());
        if (table == null) {
            throw new RuntimeException("桌号不存在");
        }
        table.setStatus("EMPTY");
        table.setCurrentOrderId(null);
        tableMapper.updateById(table);

        if (order.getMemberId() != null) {
            Member member = memberMapper.selectById(order.getMemberId());
            if (member == null) {
                throw new RuntimeException("会员不存在");
            }
            member.setTotalConsumption(member.getTotalConsumption().add(order.getActualAmount()));
            member.setPoints(member.getPoints() + order.getActualAmount().intValue());
            memberMapper.updateById(member);
        }

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

        stats.setTotalOrders(stats.getTotalOrders() + 1);
        stats.setTotalAmount(stats.getTotalAmount().add(order.getTotalAmount()));
        stats.setTotalDiscount(stats.getTotalDiscount().add(order.getDiscountAmount()));

        switch (order.getPaymentMethod()) {
            case "CASH" -> stats.setCashAmount(stats.getCashAmount().add(order.getActualAmount()));
            case "ALIPAY" -> stats.setAlipayAmount(stats.getAlipayAmount().add(order.getActualAmount()));
            case "WECHAT" -> stats.setWechatAmount(stats.getWechatAmount().add(order.getActualAmount()));
            default -> {
            }
        }

        if (order.getMemberId() != null) {
            stats.setMemberAmount(stats.getMemberAmount().add(order.getActualAmount()));
        }

        if (stats.getId() == null) {
            dailyStatisticsMapper.insert(stats);
        } else {
            dailyStatisticsMapper.updateById(stats);
        }
    }

    private OrderVO convertToVO(Order order) {
        if (order == null) {
            return null;
        }
        List<OrderVO> orderVOList = convertToVOList(Collections.singletonList(order));
        return orderVOList.isEmpty() ? null : orderVOList.get(0);
    }

    private List<OrderVO> convertToVOList(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> orderIds = orders.stream()
                .map(Order::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, List<OrderItemVO>> orderItemsMap = orderIds.isEmpty()
                ? Collections.emptyMap()
                : orderItemMapper.findByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getOrderId,
                        Collectors.mapping(item -> BeanUtil.copyProperties(item, OrderItemVO.class), Collectors.toList())
                ));

        Set<Long> memberIds = orders.stream()
                .map(Order::getMemberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> memberNameMap = memberIds.isEmpty()
                ? Collections.emptyMap()
                : memberMapper.selectBatchIds(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getName, (left, right) -> left));

        Set<Long> cashierIds = orders.stream()
                .map(Order::getCashierId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> cashierNameMap = cashierIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(cashierIds).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName, (left, right) -> left));

        return orders.stream()
                .map(order -> {
                    OrderVO vo = BeanUtil.copyProperties(order, OrderVO.class);
                    vo.setItems(orderItemsMap.getOrDefault(order.getId(), Collections.emptyList()));
                    if (order.getMemberId() != null) {
                        vo.setMemberName(memberNameMap.get(order.getMemberId()));
                    }
                    if (order.getCashierId() != null) {
                        vo.setCashierName(cashierNameMap.get(order.getCashierId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
