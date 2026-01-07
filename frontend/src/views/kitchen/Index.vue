<template>
  <div class="kitchen-index">
    <el-container>
      <el-header class="kitchen-header">
        <h1>后厨订单管理</h1>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </el-header>
      
      <el-main>
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
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getAllOrdersApi, updateOrderStatusApi } from '@/api/order'
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket'
import type { OrderVO } from '@/types/api'
import OrderList from './components/OrderList.vue'

const router = useRouter()
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
  min-height: 100vh;
  background: #f5f7fa;
}

.kitchen-header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.kitchen-header h1 {
  margin: 0;
  color: #333;
}
</style>

