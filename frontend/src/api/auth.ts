import request from '@/utils/request'
import type { ApiResponse, LoginRequest, LoginResponse } from '@/types/api'

export const loginApi = (data: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
  return request.post('/auth/login', data)
}

export const logoutApi = (): Promise<ApiResponse> => {
  return request.post('/auth/logout')
}

