<template>
  <div class="cashier-view">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="待结账订单" name="orders">
        <OrderList :orders="readyOrders" @pay="handlePay" />
      </el-tab-pane>
      <el-tab-pane label="会员管理" name="members">
        <MemberManagement />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getReadyOrdersApi, payOrderApi } from '@/api/order'
import type { OrderVO, PaymentDTO } from '@/types/api'
import OrderList from '../cashier/components/OrderList.vue'
import MemberManagement from './MemberManagement.vue'
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket'

const userStore = useUserStore()

const orders = ref<OrderVO[]>([])
const activeTab = ref('orders')

const readyOrders = computed(() => 
  orders.value.filter(o => o.status === 'READY')
)

const loadOrders = async () => {
  try {
    const res = await getReadyOrdersApi()
    orders.value = res.data
  } catch (error) {
    console.error('加载订单失败', error)
  }
}

const handlePay = async (orderId: number, paymentMethod: string) => {
  try {
    const paymentDTO: PaymentDTO = {
      paymentMethod,
      cashierId: userStore.userId || undefined
    }
    await payOrderApi(orderId, paymentDTO)
    await loadOrders()
  } catch (error: any) {
    console.error('支付失败', error)
  }
}

onMounted(() => {
  loadOrders()

  // 通过 WebSocket 实时感知新订单或状态变化
  connectWebSocket((order: OrderVO) => {
    if (order.status === 'READY') {
      const index = orders.value.findIndex(o => o.id === order.id)
      if (index > -1) {
        orders.value[index] = order
      } else {
        orders.value.push(order)
      }
    } else {
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
.cashier-view {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
}
</style>
