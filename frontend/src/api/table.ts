import request from '@/utils/request'
import type { ApiResponse, Table } from '@/types/api'

export const getTablesApi = (): Promise<ApiResponse<Table[]>> => {
  return request.get('/tables')
}

export const getTableByNumberApi = (tableNumber: string): Promise<ApiResponse<Table>> => {
  return request.get(`/tables/${tableNumber}`)
}

export const validateTableNumberApi = (tableNumber: string): Promise<ApiResponse<boolean>> => {
  return request.get(`/tables/${tableNumber}/validate`)
}

