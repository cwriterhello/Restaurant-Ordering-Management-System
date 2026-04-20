import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'
import type { Combo } from '@/types/api'

export const getCombosApi = (): Promise<ApiResponse<Combo[]>> => {
  return request.get('/combos')
}

export const getComboByIdApi = (id: number): Promise<ApiResponse<Combo>> => {
  return request.get(`/combos/${id}`)
}

export const createComboApi = (data: {
  combo: Combo,
  dishesConfig: ComboDishConfig
}): Promise<ApiResponse<ComboVO>> => {
  return request.post('/combos', data)
}

  // getCombosWithDishesApi 已被弃用，使用 getComboByIdApi 替代
  // export const getCombosWithDishesApi = (id: number): Promise<ApiResponse<ComboVO>> => {
  //   return request.get(`/combos/${id}`)
  // }

export const updateComboApi = (id: number, data: Combo): Promise<ApiResponse<Combo>> => {
  return request.put(`/combos/${id}`, data)
}

export const updateComboStatusApi = (id: number, isAvailable: number): Promise<ApiResponse<Combo>> => {
  return request.put(`/combos/${id}/status`, {}, {
    params: { isAvailable }
  })
}

export const deleteComboApi = (id: number): Promise<ApiResponse> => {
  return request.delete(`/combos/${id}`)
}

export interface ComboDishConfig {
  comboId: number
  dishes: {
    dishId: number
    quantity: number
  }[]
}

export const configureComboDishesApi = (comboId: string | number, config: ComboDishConfig): Promise<ApiResponse> => {
  return request.post(`/combos/${comboId}/dishes`, config)
}