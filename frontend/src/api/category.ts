import request from '@/utils/request'
import type { ApiResponse, Category } from '@/types/api'

export const getCategoriesApi = (): Promise<ApiResponse<Category[]>> => {
  return request.get('/categories')
}

export const getCategoryByIdApi = (id: number): Promise<ApiResponse<Category>> => {
  return request.get(`/categories/${id}`)
}

export const createCategoryApi = (data: Category): Promise<ApiResponse<Category>> => {
  return request.post('/categories', data)
}

export const updateCategoryApi = (id: number, data: Category): Promise<ApiResponse<Category>> => {
  return request.put(`/categories/${id}`, data)
}

export const deleteCategoryApi = (id: number): Promise<ApiResponse> => {
  return request.delete(`/categories/${id}`)
}

