# 餐饮门店点餐管理系统 - 前端

## 技术栈

- Vue 3 (Composition API)
- TypeScript
- Element Plus
- Vue Router
- Pinia
- Axios
- WebSocket (SockJS + STOMP)

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:5173

### 构建生产版本

```bash
npm run build
```

## 项目结构

```
frontend/
├── src/
│   ├── api/          # API接口封装
│   ├── assets/       # 静态资源
│   ├── components/   # 公共组件
│   ├── router/       # 路由配置
│   ├── stores/       # Pinia状态管理
│   ├── types/        # TypeScript类型定义
│   ├── utils/        # 工具函数
│   ├── views/        # 页面组件
│   │   ├── customer/    # 客户点餐端
│   │   ├── kitchen/     # 后厨端
│   │   ├── cashier/     # 收银端
│   │   └── admin/       # 管理员端
│   ├── App.vue
│   └── main.ts
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## 功能模块

### 客户点餐端
- 扫码/输入桌号
- 菜品分类浏览
- 购物车管理
- 订单提交

### 后厨端
- 实时订单接收（WebSocket）
- 订单状态管理
- 出餐确认

### 收银端
- 待结账订单查看
- 多种支付方式
- 会员管理

### 管理员端
- 菜品管理（增删改查、上下架）
- 营业数据统计
- 库存预警

## 默认账户

- 管理员：admin / admin123
- 收银员：cashier1 / admin123
- 后厨：kitchen1 / admin123

