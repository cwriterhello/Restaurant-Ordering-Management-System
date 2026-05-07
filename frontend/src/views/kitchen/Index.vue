<template>
  <div class="app-page kitchen-index">
    <div class="app-shell">
      <header class="app-header kitchen-header">
        <div>
          <h1 class="app-title">后厨订单管理</h1>
          <p class="app-subtitle">实时追踪待处理、制作中与已完成订单</p>
        </div>
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </header>

      <main class="app-main">
        <el-card class="module-card">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="待处理" name="PENDING">
            <OrderList :orders="pendingOrders" @update-status="handleUpdateStatus" />
          </el-tab-pane>
          <el-tab-pane label="制作中" name="PREPARING">
            <OrderList :orders="preparingOrders" @update-status="handleUpdateStatus" />
          </el-tab-pane>
          <el-tab-pane label="已完成" name="READY">
            <OrderList :orders="readyOrders" @update-status="handleUpdateStatus" />
          </el-tab-pane>
          </el-tabs>
        </el-card>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getAllOrdersApi, updateOrderStatusApi } from '@/api/order'
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket'
import type { OrderVO } from '@/types/api'
import OrderList from './components/OrderList.vue'

const userStore = useUserStore()

const orders = ref<OrderVO[]>([])
const activeTab = ref('PENDING')

const pendingOrders = computed(() => 
  orders.value.filter(o => o.status === 'PENDING' || o.status === 'CONFIRMED')
)

const preparingOrders = computed(() => 
  orders.value.filter(o => o.status === 'PREPARING')
)

const readyOrders = computed(() => 
  orders.value.filter(o => o.status === 'READY')
)

const loadOrders = async () => {
  try {
    const res = await getAllOrdersApi()
    orders.value = res.data
  } catch (error) {
    ElMessage.error('加载订单失败')
  }
}

const handleTabChange = () => {
  loadOrders()
}

const handleUpdateStatus = async (orderId: number, status: string) => {
  try {
    await updateOrderStatusApi(orderId, { status })
    ElMessage.success('订单状态更新成功')
    await loadOrders()
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  }
}

const handleLogout = () => {
  userStore.logout()
}

onMounted(() => {
  loadOrders()
  
  // 连接WebSocket接收实时订单
  connectWebSocket((order: OrderVO) => {
    const index = orders.value.findIndex(o => o.id === order.id)
    if (index > -1) {
      orders.value[index] = order
    } else {
      orders.value.push(order)
    }
    ElMessage.info(`新订单：${order.orderNumber}`)
  })
})

onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
.kitchen-index {
  display: flex;
  align-items: stretch;
}

.kitchen-header {
  border-bottom: none;
}

.kitchen-header h1 {
  margin: 0;
}
</style>

