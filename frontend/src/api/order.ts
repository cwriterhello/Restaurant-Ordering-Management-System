import request from '@/utils/request'
import type {
  ApiResponse,
  OrderVO,
  CreateOrderRequest,
  OrderStatusUpdateDTO,
  PaymentDTO,
  OnlinePaymentVO,
  OnlinePaymentStatusVO
} from '@/types/api'

export const getActiveOrdersApi = (): Promise<ApiResponse<OrderVO[]>> => {
  return request.get('/orders/active')
}

export const getAllOrdersApi = (): Promise<ApiResponse<OrderVO[]>> => {
  return request.get('/orders')
}

export const getReadyOrdersApi = (): Promise<ApiResponse<OrderVO[]>> => {
  return request.get('/orders/ready')
}

export const createOrderApi = (data: CreateOrderRequest): Promise<ApiResponse<OrderVO>> => {
  return request.post('/orders', data)
}

export const updateOrderStatusApi = (
  orderId: number, 
  data: OrderStatusUpdateDTO
): Promise<ApiResponse<OrderVO>> => {
  return request.put(`/orders/${orderId}/status`, data)
}

export const payOrderApi = (
  orderId: number,
  data: PaymentDTO
): Promise<ApiResponse<OrderVO>> => {
  return request.post(`/orders/${orderId}/pay`, data)
}

export const createOnlinePaymentApi = (
  orderId: number,
  data: PaymentDTO
): Promise<ApiResponse<OnlinePaymentVO>> => {
  return request.post(`/orders/${orderId}/online-payment`, data)
}

export const getOnlinePaymentStatusApi = (
  orderId: number,
  paymentNo: string
): Promise<ApiResponse<OnlinePaymentStatusVO>> => {
  return request.get(`/orders/${orderId}/online-payment/${paymentNo}`)
}

export const confirmOnlinePaymentApi = (
  orderId: number,
  paymentNo: string,
  data: { cashierId?: number }
): Promise<ApiResponse<OrderVO>> => {
  return request.post(`/orders/${orderId}/online-payment/${paymentNo}/confirm`, data)
}
