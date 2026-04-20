// API 响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 登录相关
export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  role: string
  realName: string
  userId: number // 添加用户ID字段
}

// 菜品相关
export interface DishVO {
  id: number
  name: string
  categoryId: number
  categoryName?: string
  description: string
  image: string
  price: number
  originalPrice?: number
  stock: number
  unit: string
  isAvailable: number
  isRecommend: number
  sortOrder: number
  createTime: string
  updateTime: string
}

export interface DishDTO {
  id?: number
  name: string
  categoryId: number
  description?: string
  image?: string
  price: number
  originalPrice?: number
  stock: number
  unit?: string
  isAvailable?: number
  isRecommend?: number
  sortOrder?: number
}

// 分类相关
export interface Category {
  id: number
  name: string
  sortOrder: number
  icon: string
  status: number
  createTime: string
  updateTime: string
}

// 套餐相关
export interface Combo {
  id: number
  name: string
  description: string
  image: string
  price: number
  originalPrice?: number
  isAvailable: number
  sortOrder: number
  createTime: string
  updateTime: string
}

// 套餐视图对象
export interface ComboVO extends Combo {
  dishes?: Array<{
    dishId: number
    dishName: string
    price: number
    quantity: number
  }>
}

// 订单相关
export interface OrderVO {
  id: number
  orderNumber: string
  tableId: number
  tableNumber: string
  memberId?: number
  memberPhone?: string
  memberName?: string
  totalAmount: number
  discountAmount: number
  actualAmount: number
  status: string
  paymentMethod?: string
  paymentTime?: string
  cashierId?: number
  cashierName?: string
  remark?: string
  items: OrderItemVO[]
  createTime: string
  updateTime: string
}

export interface OrderItemVO {
  id: number
  orderId: number
  itemType: string
  itemId: number
  itemName: string
  price: number
  quantity: number
  subtotal: number
  status: string
  createTime: string
}

export interface CreateOrderRequest {
  tableNumber: string
  memberPhone?: string
  remark?: string
  items: OrderItemDTO[]
}

export interface OrderItemDTO {
  itemType: string
  itemId: number
  quantity: number
}

export interface OrderStatusUpdateDTO {
  status: string
}

export interface PaymentDTO {
  paymentMethod: string
  cashierId?: number
}

// 桌号相关
export interface Table {
  id: number
  tableNumber: string
  qrCode?: string
  capacity: number
  status: string
  currentOrderId?: number
  createTime: string
  updateTime: string
}

// 会员相关
export interface Member {
  id: number
  phone: string
  name?: string
  level: string
  discount: number
  points: number
  totalConsumption: number
  createTime: string
  updateTime: string
}

// 库存预警
export interface StockAlert {
  id: number
  dishId: number
  dishName: string
  currentStock: number
  alertThreshold: number
  status: string
  createTime: string
  resolveTime?: string
}

// 统计数据
export interface DailyStatistics {
  id: number
  statDate: string
  totalOrders: number
  totalAmount: number
  totalDiscount: number
  cashAmount: number
  alipayAmount: number
  wechatAmount: number
  memberAmount: number
  createTime: string
  updateTime: string
}