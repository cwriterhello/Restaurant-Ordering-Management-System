package com.example.demo.service;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OnlinePaymentConfirmDTO;
import com.example.demo.dto.OnlinePaymentSession;
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
import com.example.demo.vo.OnlinePaymentStatusVO;
import com.example.demo.vo.OnlinePaymentVO;
import com.example.demo.vo.OrderVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {
    private static final Set<String> ONLINE_PAYMENT_METHODS = Set.of("ALIPAY", "WECHAT");
    private static final String ONLINE_PAYMENT_KEY_PREFIX = "online:payment:";

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

    @Autowired
    private RedisService redisService;

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private WechatPayService wechatPayService;

    @Value("${payment.online.session-expire-minutes:15}")
    private long onlinePaymentExpireMinutes;

    @Value("${payment.online.alipay-qr-content:}")
    private String alipayQrContent;

    @Value("${payment.online.wechat-qr-content:}")
    private String wechatQrContent;

    @Value("${payment.online.mock-host:http://localhost:8082}")
    private String onlinePaymentMockHost;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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

    /**
     * 创建在线支付订单(集成真实第三方支付)
     * 
     * <p>业务流程:</p>
     * <ol>
     *   <li>验证订单状态和支付方式合法性</li>
     *   <li>调用支付宝/微信API创建支付订单</li>
     *   <li>生成二维码内容(支付宝返回HTML表单,微信返回code_url)</li>
     *   <li>保存支付会话到Redis用于后续状态查询</li>
     * </ol>
     * 
     * @param orderId 订单ID
     * @param paymentDTO 支付信息(包含支付方式、收银员ID)
     * @return 在线支付VO对象(包含二维码内容、支付流水号等)
     */
    public OnlinePaymentVO createOnlinePayment(Long orderId, PaymentDTO paymentDTO) {
        String paymentMethod = paymentDTO.getPaymentMethod();
        if (!ONLINE_PAYMENT_METHODS.contains(paymentMethod)) {
            throw new RuntimeException("在线支付仅支持支付宝或微信");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"READY".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法发起在线支付");
        }

        long expireMinutes = Math.max(1, onlinePaymentExpireMinutes);
        LocalDateTime now = LocalDateTime.now();

        // 生成内部支付流水号
        String paymentNo = generatePaymentNo();
        
        // 调用第三方支付API创建订单
        String qrCodeContent;
        try {
            if ("ALIPAY".equals(paymentMethod)) {
                // 支付宝: 生成PC网站支付表单HTML
                qrCodeContent = alipayService.createPagePayment(
                    order.getOrderNumber(),
                    order.getActualAmount(),
                    "餐厅订单-" + order.getTableNumber(),
                    "桌号: " + order.getTableNumber() + ", 订单号: " + order.getOrderNumber()
                );
            } else {
                // 微信: 生成Native扫码支付链接
                qrCodeContent = wechatPayService.createNativePayment(
                    order.getOrderNumber(),
                    order.getActualAmount(),
                    "餐厅订单-" + order.getTableNumber()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("调用第三方支付接口失败: " + e.getMessage());
        }

        // 构建支付会话对象
        OnlinePaymentSession session = new OnlinePaymentSession();
        session.setPaymentNo(paymentNo);
        session.setOrderId(order.getId());
        session.setOrderNumber(order.getOrderNumber());
        session.setTableNumber(order.getTableNumber());
        session.setPaymentMethod(paymentMethod);
        session.setAmount(order.getActualAmount());
        session.setStatus("CREATED");
        session.setCashierId(paymentDTO.getCashierId());
        session.setCreateTime(now);
        session.setExpireTime(now.plusMinutes(expireMinutes));
        session.setQrCodeContent(qrCodeContent);

        // 保存到Redis(用于前端轮询查询状态)
        redisService.set(buildOnlinePaymentKey(session.getPaymentNo()), session, expireMinutes, TimeUnit.MINUTES);
        
        log.info("在线支付订单创建成功: paymentNo={}, method={}, amount={}", 
                paymentNo, paymentMethod, order.getActualAmount());
        return convertOnlinePaymentSession(session);
    }

    public OnlinePaymentStatusVO getOnlinePaymentStatus(Long orderId, String paymentNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        OnlinePaymentSession session = getOnlinePaymentSession(paymentNo);
        if (session == null) {
            return buildNotFoundOrPaidStatus(orderId, paymentNo, order);
        }
        if (!Objects.equals(session.getOrderId(), orderId)) {
            throw new RuntimeException("在线支付单与订单不匹配");
        }

        if ("CREATED".equals(session.getStatus())
                && session.getExpireTime() != null
                && LocalDateTime.now().isAfter(session.getExpireTime())) {
            session.setStatus("EXPIRED");
            redisService.set(buildOnlinePaymentKey(paymentNo), session, 10, TimeUnit.MINUTES);
        }

        OnlinePaymentStatusVO statusVO = convertOnlinePaymentStatus(session);
        if ("PAID".equals(order.getStatus())) {
            statusVO.setPaymentStatus("SUCCESS");
            statusVO.setPaidTime(order.getPaymentTime());
        }
        return statusVO;
    }

    @Transactional
    public OrderVO confirmOnlinePayment(Long orderId, String paymentNo, OnlinePaymentConfirmDTO confirmDTO) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        OnlinePaymentSession session = getOnlinePaymentSession(paymentNo);
        if (session == null) {
            if ("PAID".equals(order.getStatus())) {
                return convertToVO(order);
            }
            throw new RuntimeException("在线支付单不存在或已过期，请重新发起支付");
        }
        if (!Objects.equals(session.getOrderId(), orderId)) {
            throw new RuntimeException("在线支付单与订单不匹配");
        }
        if ("EXPIRED".equals(session.getStatus())) {
            throw new RuntimeException("在线支付单已过期，请重新发起支付");
        }

        Long cashierId = confirmDTO != null && confirmDTO.getCashierId() != null
                ? confirmDTO.getCashierId()
                : session.getCashierId();

        if (!"PAID".equals(order.getStatus())) {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setPaymentMethod(session.getPaymentMethod());
            paymentDTO.setCashierId(cashierId);
            payOrder(orderId, paymentDTO);
        }

        session.setStatus("SUCCESS");
        session.setCashierId(cashierId);
        session.setPaidTime(LocalDateTime.now());
        redisService.set(buildOnlinePaymentKey(paymentNo), session, 1, TimeUnit.DAYS);
        return convertToVO(orderMapper.selectById(orderId));
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
        if ("PAID".equals(order.getStatus())) {
            return convertToVO(order);
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

        webSocketService.sendOrderStatusUpdate(order);
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

    private String generatePaymentNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    private String resolveOnlineQrCodeContent(Order order, OnlinePaymentSession session) {
        String template = "ALIPAY".equals(session.getPaymentMethod()) ? alipayQrContent : wechatQrContent;
        if (StringUtils.hasText(template)) {
            return template
                    .replace("{orderNo}", session.getOrderNumber())
                    .replace("{paymentNo}", session.getPaymentNo())
                    .replace("{amount}", session.getAmount().stripTrailingZeros().toPlainString())
                    .replace("{tableNo}", order.getTableNumber());
        }

        String mockHost = StringUtils.hasText(onlinePaymentMockHost) ? onlinePaymentMockHost : "http://localhost:8082";
        return mockHost + "/api/orders/" + session.getOrderId() + "/online-payment/" + session.getPaymentNo()
                + "?method=" + session.getPaymentMethod()
                + "&amount=" + session.getAmount().stripTrailingZeros().toPlainString();
    }

    private String buildOnlinePaymentKey(String paymentNo) {
        return ONLINE_PAYMENT_KEY_PREFIX + paymentNo;
    }

    private OnlinePaymentSession getOnlinePaymentSession(String paymentNo) {
        Object cached = redisService.get(buildOnlinePaymentKey(paymentNo));
        if (cached == null) {
            return null;
        }
        if (cached instanceof OnlinePaymentSession session) {
            return session;
        }
        return objectMapper.convertValue(cached, OnlinePaymentSession.class);
    }

    private OnlinePaymentVO convertOnlinePaymentSession(OnlinePaymentSession session) {
        OnlinePaymentVO onlinePaymentVO = new OnlinePaymentVO();
        onlinePaymentVO.setPaymentNo(session.getPaymentNo());
        onlinePaymentVO.setOrderId(session.getOrderId());
        onlinePaymentVO.setOrderNumber(session.getOrderNumber());
        onlinePaymentVO.setTableNumber(session.getTableNumber());
        onlinePaymentVO.setPaymentMethod(session.getPaymentMethod());
        onlinePaymentVO.setAmount(session.getAmount());
        onlinePaymentVO.setQrCodeContent(session.getQrCodeContent());
        onlinePaymentVO.setStatus(session.getStatus());
        onlinePaymentVO.setCreateTime(session.getCreateTime());
        onlinePaymentVO.setExpireTime(session.getExpireTime());
        return onlinePaymentVO;
    }

    private OnlinePaymentStatusVO convertOnlinePaymentStatus(OnlinePaymentSession session) {
        OnlinePaymentStatusVO statusVO = new OnlinePaymentStatusVO();
        statusVO.setPaymentNo(session.getPaymentNo());
        statusVO.setOrderId(session.getOrderId());
        statusVO.setPaymentMethod(session.getPaymentMethod());
        statusVO.setAmount(session.getAmount());
        statusVO.setPaymentStatus(session.getStatus());
        statusVO.setExpireTime(session.getExpireTime());
        statusVO.setPaidTime(session.getPaidTime());
        return statusVO;
    }

    private OnlinePaymentStatusVO buildNotFoundOrPaidStatus(Long orderId, String paymentNo, Order order) {
        OnlinePaymentStatusVO statusVO = new OnlinePaymentStatusVO();
        statusVO.setPaymentNo(paymentNo);
        statusVO.setOrderId(orderId);
        statusVO.setPaymentMethod(order.getPaymentMethod());
        statusVO.setAmount(order.getActualAmount());
        if ("PAID".equals(order.getStatus())) {
            statusVO.setPaymentStatus("SUCCESS");
            statusVO.setPaidTime(order.getPaymentTime());
        } else {
            statusVO.setPaymentStatus("NOT_FOUND");
        }
        return statusVO;
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
    
    /**
     * 处理第三方支付成功回调(由PaymentNotifyController调用)
     * 
     * <p>幂等性保证:</p>
     * <ul>
     *   <li>如果订单已支付,直接返回不重复处理</li>
     *   <li>防止第三方平台重复推送通知导致数据错误</li>
     * </ul>
     * 
     * @param orderNumber 商户订单号
     * @param paymentMethod 支付方式(ALIPAY/WECHAT)
     */
    @Transactional
    public void handlePaymentSuccessByOrderNumber(String orderNumber, String paymentMethod) {
        // 根据订单号查找订单
        Order order = orderMapper.findByOrderNumber(orderNumber);
        if (order == null) {
            log.error("订单不存在: {}", orderNumber);
            throw new RuntimeException("订单不存在");
        }
        
        // 幂等性检查:如果已支付则直接返回
        if ("PAID".equals(order.getStatus())) {
            log.info("订单已支付,跳过处理: {}", orderNumber);
            return;
        }
        
        // 构建PaymentDTO并调用payOrder方法
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentMethod(paymentMethod);
        paymentDTO.setCashierId(order.getCashierId()); // 使用订单关联的收银员
        
        // 更新订单状态、释放桌台、累计会员积分、更新统计数据
        payOrder(order.getId(), paymentDTO);
        
        log.info("第三方支付成功处理完成: orderNo={}, method={}", orderNumber, paymentMethod);
    }
}
