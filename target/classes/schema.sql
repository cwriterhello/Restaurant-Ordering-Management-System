-- 餐饮门店点餐管理系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS restaurant_order_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE restaurant_order_system;

-- 用户表（管理员、收银员、后厨等）
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：ADMIN-管理员, CASHIER-收银员, KITCHEN-后厨',
  `phone` VARCHAR(20) COMMENT '手机号',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 桌号表
CREATE TABLE IF NOT EXISTS `tables` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `table_number` VARCHAR(20) NOT NULL UNIQUE COMMENT '桌号',
  `qr_code` VARCHAR(255) COMMENT '二维码URL',
  `capacity` INT DEFAULT 4 COMMENT '容纳人数',
  `status` VARCHAR(20) DEFAULT 'EMPTY' COMMENT '状态：EMPTY-空桌, OCCUPIED-已占用, RESERVED-已预订',
  `current_order_id` BIGINT COMMENT '当前订单ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌号表';

-- 菜品分类表
CREATE TABLE IF NOT EXISTS `categories` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `icon` VARCHAR(255) COMMENT '图标URL',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- 菜品表
CREATE TABLE IF NOT EXISTS `dishes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `description` TEXT COMMENT '菜品描述',
  `image` VARCHAR(255) COMMENT '图片URL',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `original_price` DECIMAL(10,2) COMMENT '原价（用于折扣显示）',
  `stock` INT DEFAULT 0 COMMENT '库存数量',
  `unit` VARCHAR(20) DEFAULT '份' COMMENT '单位',
  `is_available` TINYINT DEFAULT 1 COMMENT '是否上架：1-上架，0-下架',
  `is_recommend` TINYINT DEFAULT 0 COMMENT '是否推荐：1-是，0-否',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- 套餐表
CREATE TABLE IF NOT EXISTS `combos` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
  `description` TEXT COMMENT '套餐描述',
  `image` VARCHAR(255) COMMENT '图片URL',
  `price` DECIMAL(10,2) NOT NULL COMMENT '套餐价格',
  `original_price` DECIMAL(10,2) COMMENT '原价',
  `is_available` TINYINT DEFAULT 1 COMMENT '是否上架：1-上架，0-下架',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';

-- 套餐菜品关联表
CREATE TABLE IF NOT EXISTS `combo_dishes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `combo_id` BIGINT NOT NULL COMMENT '套餐ID',
  `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
  `quantity` INT DEFAULT 1 COMMENT '数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`combo_id`) REFERENCES `combos`(`id`),
  FOREIGN KEY (`dish_id`) REFERENCES `dishes`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐菜品关联表';

-- 会员表
CREATE TABLE IF NOT EXISTS `members` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `phone` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
  `name` VARCHAR(50) COMMENT '姓名',
  `level` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '会员等级：NORMAL-普通, SILVER-银卡, GOLD-金卡, PLATINUM-白金',
  `discount` DECIMAL(5,2) DEFAULT 1.00 COMMENT '折扣率（0-1之间）',
  `points` INT DEFAULT 0 COMMENT '积分',
  `total_consumption` DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计消费',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `order_number` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
  `table_id` BIGINT NOT NULL COMMENT '桌号ID',
  `table_number` VARCHAR(20) NOT NULL COMMENT '桌号',
  `member_id` BIGINT COMMENT '会员ID',
  `member_phone` VARCHAR(20) COMMENT '会员手机号',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '折扣金额',
  `actual_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待处理, CONFIRMED-已确认, PREPARING-制作中, READY-已完成, PAID-已支付, CANCELLED-已取消',
  `payment_method` VARCHAR(20) COMMENT '支付方式：CASH-现金, ALIPAY-支付宝, WECHAT-微信, MEMBER-会员卡',
  `payment_time` DATETIME COMMENT '支付时间',
  `cashier_id` BIGINT COMMENT '收银员ID',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS `order_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `item_type` VARCHAR(20) NOT NULL COMMENT '类型：DISH-菜品, COMBO-套餐',
  `item_id` BIGINT NOT NULL COMMENT '菜品或套餐ID',
  `item_name` VARCHAR(100) NOT NULL COMMENT '菜品或套餐名称',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `quantity` INT NOT NULL COMMENT '数量',
  `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待制作, PREPARING-制作中, READY-已完成',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 库存预警表
CREATE TABLE IF NOT EXISTS `stock_alerts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
  `dish_name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
  `current_stock` INT NOT NULL COMMENT '当前库存',
  `alert_threshold` INT DEFAULT 10 COMMENT '预警阈值',
  `status` VARCHAR(20) DEFAULT 'ALERT' COMMENT '状态：ALERT-预警, RESOLVED-已解决',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `resolve_time` DATETIME COMMENT '解决时间',
  FOREIGN KEY (`dish_id`) REFERENCES `dishes`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警表';

-- 营业数据统计表（可按日统计）
CREATE TABLE IF NOT EXISTS `daily_statistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `stat_date` DATE NOT NULL UNIQUE COMMENT '统计日期',
  `total_orders` INT DEFAULT 0 COMMENT '订单总数',
  `total_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '总营业额',
  `total_discount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '总折扣额',
  `cash_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '现金支付',
  `alipay_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '支付宝支付',
  `wechat_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '微信支付',
  `member_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '会员卡支付',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营业数据统计表';

-- 插入初始数据

-- 注意：用户账户将通过应用启动时的DataInitializer自动创建（密码会自动加密）
-- 如果需要手动插入，请使用BCrypt加密后的密码

-- 插入桌号数据
INSERT INTO `tables` (`table_number`, `capacity`, `status`) VALUES
('A01', 4, 'EMPTY'),
('A02', 4, 'EMPTY'),
('A03', 6, 'EMPTY'),
('B01', 2, 'EMPTY'),
('B02', 4, 'EMPTY'),
('B03', 8, 'EMPTY');

-- 插入菜品分类
INSERT INTO `categories` (`name`, `sort_order`, `status`) VALUES
('热菜', 1, 1),
('凉菜', 2, 1),
('汤品', 3, 1),
('主食', 4, 1),
('饮品', 5, 1),
('甜品', 6, 1);

-- 插入示例菜品
INSERT INTO `dishes` (`name`, `category_id`, `description`, `price`, `stock`, `is_available`, `is_recommend`, `sort_order`) VALUES
('宫保鸡丁', 1, '经典川菜，鸡肉配花生米', 38.00, 50, 1, 1, 1),
('麻婆豆腐', 1, '四川传统名菜', 28.00, 50, 1, 1, 2),
('糖醋里脊', 1, '酸甜可口', 42.00, 50, 1, 0, 3),
('凉拌黄瓜', 2, '清爽开胃', 12.00, 50, 1, 0, 1),
('口水鸡', 2, '麻辣鲜香', 35.00, 50, 1, 1, 2),
('西红柿鸡蛋汤', 3, '家常汤品', 18.00, 50, 1, 0, 1),
('米饭', 4, '白米饭', 3.00, 100, 1, 0, 1),
('可乐', 5, '冰镇可乐', 8.00, 100, 1, 0, 1),
('提拉米苏', 6, '意式甜品', 25.00, 30, 1, 1, 1);

