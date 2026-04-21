<template>
  <div class="kitchen-view">
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllOrdersApi, updateOrderStatusApi } from '@/api/order'
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket'
import type { OrderVO } from '@/types/api'
import OrderList from '../kitchen/components/OrderList.vue'

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
.kitchen-view {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
}
</style>
