import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi } from '@/api/auth'
import router from '@/router'
import type { LoginRequest } from '@/types/api'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const username = ref<string>(localStorage.getItem('username') || '')
  const role = ref<string>(localStorage.getItem('role') || '')
  const realName = ref<string>(localStorage.getItem('realName') || '')
  const userId = ref<number>(Number(localStorage.getItem('userId') || 0))

  const login = async (loginForm: LoginRequest) => {
    const res = await loginApi(loginForm)
    token.value = res.data.token
    username.value = res.data.username
    role.value = res.data.role
    realName.value = res.data.realName
    userId.value = res.data.userId // 存储用户ID
    
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('role', res.data.role)
    localStorage.setItem('realName', res.data.realName)
    localStorage.setItem('userId', res.data.userId.toString()) // 存储用户ID
    
    // 根据角色跳转
    try {
      if (res.data.role === 'ADMIN') {
        await router.push('/admin')
      } else if (res.data.role === 'CASHIER') {
        await router.push('/cashier')
      } else if (res.data.role === 'KITCHEN') {
        await router.push('/kitchen')
      } else {
        await router.push('/customer')
      }
    } catch (error) {
      // 忽略重复导航错误
      if (error instanceof Error && !error.message.includes('Redirected from')) {
        console.error('Navigation error:', error)
      }
    }
  }

  const logout = () => {
    token.value = ''
    username.value = ''
    role.value = ''
    realName.value = ''
    userId.value = 0
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    localStorage.removeItem('realName')
    localStorage.removeItem('userId')
    router.push('/login')
  }

  return {
    token,
    username,
    role,
    realName,
    userId,
    login,
    logout
  }
})