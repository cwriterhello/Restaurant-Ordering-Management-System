import request from '@/utils/request'

// 获取所有用户
export const getAllUsersApi = () => {
  return request({
    url: '/users',
    method: 'get'
  })
}

// 创建用户
export const createUserApi = (data: any) => {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

// 更新用户
export const updateUserApi = (id: number, data: any) => {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export const deleteUserApi = (id: number) => {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

// 重置用户密码
export const resetUserPasswordApi = (id: number, data: { newPassword: string }) => {
  return request({
    url: `/users/${id}/reset-password`,
    method: 'post',
    data
  })
}