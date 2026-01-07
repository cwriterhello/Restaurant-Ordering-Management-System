# 餐饮门店点餐管理系统

## 项目简介

本系统以 Vue3 为核心前端框架，搭配 Element Plus 组件库、Spring Boot 后端及 MySQL 数据库，构建适配餐饮门店的全流程点餐管理系统。

## 技术栈

### 前端
- Vue 3 (Composition API)
- Element Plus
- Vue Router
- Pinia (状态管理)
- Axios
- WebSocket (实时通信)

### 后端
- Spring Boot 4.0.1
- MyBatis-Plus
- Spring WebSocket
- MySQL 8.0+
- JWT (身份认证)
- BCrypt (密码加密)

## 功能模块

### 客户点餐端（移动端）
- 桌号扫码点餐
- 菜品分类展示
- 购物车管理
- 订单提交

### 后厨端
- 订单实时接收（WebSocket）
- 订单状态管理
- 出餐确认

### 收银端
- 订单查看
- 结账收银
- 会员折扣管理
- 营业数据统计

### 管理员端
- 菜品管理（上下架）
- 套餐配置
- 库存预警
- 数据可视化

## 项目结构

```
demo/
├── frontend/            # Vue3 前端项目
│   ├── src/
│   │   ├── api/        # API接口封装
│   │   ├── router/     # 路由配置
│   │   ├── stores/     # Pinia状态管理
│   │   ├── types/      # TypeScript类型
│   │   ├── utils/      # 工具函数
│   │   └── views/      # 页面组件
│   ├── package.json
│   └── vite.config.ts
├── src/main/java/com/example/demo/
│   ├── config/          # 配置类（WebSocket、CORS、Security等）
│   ├── controller/       # REST API控制器
│   ├── dto/             # 数据传输对象
│   ├── entity/          # 实体类（MyBatis-Plus）
│   ├── mapper/          # 数据访问层（MyBatis-Plus）
│   ├── service/         # 业务逻辑层
│   ├── util/            # 工具类（JWT等）
│   └── vo/              # 视图对象
├── src/main/resources/
│   └── application.properties  # 应用配置
├── database/
│   └── init.sql         # 数据库初始化脚本
└── pom.xml              # Maven依赖配置
```

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+ (前端开发)

### 2. 数据库初始化
```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source database/init.sql
```

或直接执行：
```bash
mysql -u root -p < database/init.sql
```

### 3. 配置数据库连接
编辑 `src/main/resources/application.properties`，修改数据库连接信息：
```properties
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. 启动后端服务
```bash
# 使用Maven启动
mvn spring-boot:run

# 或使用IDE直接运行 DemoApplication.java
```

后端服务将在 `http://localhost:8080` 启动

### 4. 启动前端服务
```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

### 5. API接口文档

#### 认证接口
- `POST /api/auth/login` - 用户登录

#### 菜品接口
- `GET /api/dishes` - 获取所有可用菜品
- `GET /api/dishes/category/{categoryId}` - 按分类获取菜品
- `GET /api/dishes/recommend` - 获取推荐菜品
- `GET /api/dishes/{id}` - 获取菜品详情
- `POST /api/dishes` - 创建菜品（管理员）
- `PUT /api/dishes/{id}` - 更新菜品（管理员）
- `PUT /api/dishes/{id}/status` - 更新菜品上下架状态（管理员）

#### 分类接口
- `GET /api/categories` - 获取所有可用分类
- `POST /api/categories` - 创建分类（管理员）
- `PUT /api/categories/{id}` - 更新分类（管理员）

#### 订单接口
- `POST /api/orders` - 创建订单
- `GET /api/orders/active` - 获取活跃订单（后厨）
- `PUT /api/orders/{id}/status` - 更新订单状态
- `POST /api/orders/{id}/pay` - 订单支付（收银）

#### 桌号接口
- `GET /api/tables` - 获取所有桌号
- `GET /api/tables/{tableNumber}` - 获取桌号信息

#### 会员接口
- `GET /api/members/phone/{phone}` - 根据手机号查询会员
- `POST /api/members` - 创建会员

#### 统计接口
- `GET /api/statistics/daily` - 获取营业数据统计
- `GET /api/statistics/stock-alerts` - 获取库存预警

### 6. WebSocket实时通信

WebSocket端点：`/ws`

订阅主题：
- `/topic/kitchen/orders` - 后厨订单推送
- `/topic/order/status/{orderId}` - 订单状态更新
- `/topic/admin/stock-alerts` - 库存预警推送

## 默认账户

数据库初始化后会创建以下测试账户（密码均为：admin123）：
- 管理员：`admin` / `admin123`
- 收银员：`cashier1` / `admin123`
- 后厨：`kitchen1` / `admin123`

## 开发说明

### 密码加密
系统使用BCrypt进行密码加密，初始化脚本中的密码为示例密码（admin123的BCrypt哈希值）。

### 订单流程
1. 客户扫码点餐 → 创建订单（状态：PENDING）
2. 后厨接收订单 → 确认订单（状态：CONFIRMED）
3. 后厨开始制作 → 更新状态（状态：PREPARING）
4. 后厨完成制作 → 更新状态（状态：READY）
5. 收银员结账 → 支付订单（状态：PAID）

### 库存预警
当菜品库存低于10份时，系统会自动创建库存预警记录。

## 后续开发

- [ ] 前端Vue3项目搭建
- [ ] 扫码点餐功能实现
- [ ] 后厨实时订单展示
- [ ] 收银端界面开发
- [ ] 管理员端功能完善
- [ ] 数据可视化图表
- [ ] 套餐管理功能
- [ ] 文件上传功能（菜品图片）
