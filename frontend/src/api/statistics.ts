import request from '@/utils/request'
import type { ApiResponse, StockAlert, DailyStatistics } from '@/types/api'

export const getDailyStatisticsApi = (startDate?: string, endDate?: string): Promise<ApiResponse<DailyStatistics[]>> => {
  return request.get('/statistics/daily', { params: { startDate, endDate } })
}

export const getStockAlertsApi = (): Promise<ApiResponse<StockAlert[]>> => {
  return request.get('/statistics/stock-alerts')
}

