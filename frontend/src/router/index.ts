import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/customer',
    name: 'Customer',
    component: () => import('@/views/customer/Index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/customer/table/:tableNumber',
    name: 'CustomerTable',
    component: () => import('@/views/customer/TableOrder.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/kitchen',
    name: 'Kitchen',
    component: () => import('@/views/kitchen/Index.vue'),
    meta: { requiresAuth: true, roles: ['KITCHEN'] }
  },
  {
    path: '/cashier',
    name: 'Cashier',
    component: () => import('@/views/cashier/Index.vue'),
    meta: { requiresAuth: true, roles: ['CASHIER', 'ADMIN'] }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/Index.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/',
    redirect: '/customer'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth) {
    if (!userStore.token) {
      next('/login')
    } else if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router

