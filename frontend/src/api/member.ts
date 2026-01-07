import request from '@/utils/request'
import type { ApiResponse, Member } from '@/types/api'

export const getMemberByPhoneApi = (phone: string): Promise<ApiResponse<Member>> => {
  return request.get(`/members/phone/${phone}`)
}

export const getMembersApi = (): Promise<ApiResponse<Member[]>> => {
  return request.get('/members')
}

export const createMemberApi = (phone: string, name?: string): Promise<ApiResponse<Member>> => {
  return request.post('/members', null, { params: { phone, name } })
}

export const updateMemberApi = (
  id: number, 
  data: Partial<Pick<Member, 'name' | 'level' | 'discount'>>): Promise<ApiResponse<Member>> => {
  return request.put(`/members/${id}`, null, { 
    params: { 
      name: data.name,
      level: data.level,
      discount: data.discount 
    } 
  })
}