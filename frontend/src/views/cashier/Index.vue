<template>
  <div class="app-page cashier-index">
    <div class="app-shell">
      <header class="app-header cashier-header">
        <div>
          <h1 class="app-title">收银管理</h1>
          <p class="app-subtitle">快速完成结账并维护会员信息</p>
        </div>
        <div class="header-actions">
          <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="app-main">
        <el-card class="module-card">
          <el-tabs v-model="activeTab">
          <el-tab-pane label="待结账订单" name="orders">
            <OrderList
              :orders="readyOrders"
              :cashier-id="userStore.userId || undefined"
              @pay="handlePay"
              @paid="loadOrders"
            />
          </el-tab-pane>
          <el-tab-pane label="会员管理" name="members">
            <MemberManagement />
          </el-tab-pane>
          </el-tabs>
        </el-card>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getReadyOrdersApi, payOrderApi } from '@/api/order'
import type { OrderVO, PaymentDTO } from '@/types/api'
import OrderList from './components/OrderList.vue'
import MemberManagement from './components/MemberManagement.vue'
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket'

const userStore = useUserStore()

const orders = ref<OrderVO[]>([])
const activeTab = ref('orders')

const readyOrders = computed(() => 
  orders.value.filter(o => o.status === 'READY')
)

const loadOrders = async () => {
  try {
    const res = await getReadyOrdersApi() // 使用新的API获取待结账订单
    orders.value = res.data
  } catch (error) {
    console.error('加载订单失败', error)
  }
}

const handlePay = async (orderId: number, paymentMethod: string) => {
  try {
    const paymentDTO: PaymentDTO = {
      paymentMethod,
      cashierId: userStore.userId || undefined // 使用实际的用户ID
    }
    await payOrderApi(orderId, paymentDTO)
    await loadOrders()
  } catch (error: any) {
    console.error('支付失败', error)
  }
}

const handleLogout = () => {
  userStore.logout()
}

onMounted(() => {
  loadOrders()

  // 通过 WebSocket 实时感知新订单或状态变化
  connectWebSocket((order: OrderVO) => {
    // 只关注已完成待结账的订单
    if (order.status === 'READY') {
      const index = orders.value.findIndex(o => o.id === order.id)
      if (index > -1) {
        orders.value[index] = order
      } else {
        orders.value.push(order)
      }
    } else {
      // 其它状态从列表移除（例如已支付）
      const index = orders.value.findIndex(o => o.id === order.id)
      if (index > -1 && order.status !== 'READY') {
        orders.value.splice(index, 1)
      }
    }
  })
})

onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
.cashier-index {
  display: flex;
  align-items: stretch;
}

.cashier-header {
  border-bottom: none;
}

.cashier-header h1 {
  margin: 0;
}
</style>

