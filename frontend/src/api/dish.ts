import request from '@/utils/request'
import type { ApiResponse, DishVO, DishDTO } from '@/types/api'

export const getDishesApi = (): Promise<ApiResponse<DishVO[]>> => {
  return request.get('/dishes')
}

// 为管理员提供获取所有菜品（包括下架）的API
export const getAllDishesApi = (): Promise<ApiResponse<DishVO[]>> => {
  return request.get('/dishes/all')
}

export const getDishesByCategoryApi = (categoryId: number): Promise<ApiResponse<DishVO[]>> => {
  return request.get(`/dishes/category/${categoryId}`)
}

export const getRecommendDishesApi = (): Promise<ApiResponse<DishVO[]>> => {
  return request.get('/dishes/recommend')
}

export const getDishByIdApi = (id: number): Promise<ApiResponse<DishVO>> => {
  return request.get(`/dishes/${id}`)
}

export const createDishApi = (data: DishDTO): Promise<ApiResponse<DishVO>> => {
  return request.post('/dishes', data)
}

export const updateDishApi = (id: number, data: DishDTO): Promise<ApiResponse<DishVO>> => {
  return request.put(`/dishes/${id}`, data)
}

export const updateDishStatusApi = (id: number, isAvailable: number): Promise<ApiResponse<DishVO>> => {
  return request.put(`/dishes/${id}/status`, null, { params: { isAvailable } })
}

export const deleteDishApi = (id: number): Promise<ApiResponse> => {
  return request.delete(`/dishes/${id}`)
}