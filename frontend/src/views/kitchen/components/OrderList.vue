<template>
  <div class="order-list">
    <el-empty v-if="orders.length === 0" description="暂无订单" />
    <div v-else class="orders-container">
      <el-card
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        :class="getStatusClass(order.status)"
      >
        <template #header>
          <div class="order-header">
            <div>
              <span class="order-number">订单号：{{ order.orderNumber }}</span>
              <el-tag :type="getStatusType(order.status)" size="small">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
            <div class="order-time">
              {{ formatTime(order.createTime) }}
            </div>
          </div>
        </template>
        
        <div class="order-content">
          <div class="order-info">
            <p><strong>桌号：</strong>{{ order.tableNumber }}</p>
            <p v-if="order.remark"><strong>备注：</strong>{{ order.remark }}</p>
          </div>
          
          <div class="order-items">
            <div
              v-for="item in order.items"
              :key="item.id"
              class="order-item"
            >
              <span>{{ item.itemName }} × {{ item.quantity }}</span>
              <span>¥{{ item.subtotal.toFixed(2) }}</span>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-total">
              <span>总计：</span>
              <span class="total-amount">¥{{ order.actualAmount.toFixed(2) }}</span>
            </div>
            <div class="order-actions">
              <el-button
                v-if="order.status === 'PENDING' || order.status === 'CONFIRMED'"
                type="primary"
                @click="handleStatusChange(order.id, 'PREPARING')"
              >
                开始制作
              </el-button>
              <el-button
                v-if="order.status === 'PREPARING'"
                type="success"
                @click="handleStatusChange(order.id, 'READY')"
              >
                完成制作
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import type { OrderVO } from '@/types/api'

defineProps<{
  orders: OrderVO[]
}>()

const emit = defineEmits<{
  'update-status': [orderId: number, status: string]
}>()

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': '待处理',
    'CONFIRMED': '已确认',
    'PREPARING': '制作中',
    'READY': '已完成',
    'PAID': '已支付',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'PENDING': 'warning',
    'CONFIRMED': 'info',
    'PREPARING': 'primary',
    'READY': 'success',
    'PAID': 'success',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || ''
}

const getStatusClass = (status: string) => {
  return `status-${status.toLowerCase()}`
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

const handleStatusChange = (orderId: number, status: string) => {
  const statusText = getStatusText(status)
  ElMessageBox.confirm(
    `确认将订单状态更新为"${statusText}"？`,
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    emit('update-status', orderId, status)
  }).catch(() => {})
}
</script>

<style scoped>
.order-list {
  padding: 8px 0 0;
}

.orders-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 14px;
}

.order-card {
  border-radius: 14px;
  transition: transform 0.2s;
}

.order-card:hover {
  transform: translateY(-3px);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-number {
  font-weight: bold;
  margin-right: 10px;
}

.order-time {
  font-size: 12px;
  color: #707f82;
}

.order-content {
  padding: 10px 0;
}

.order-info {
  margin-bottom: 15px;
}

.order-info p {
  margin: 5px 0;
  font-size: 14px;
}

.order-items {
  margin: 15px 0;
  padding: 10px;
  background: rgba(247, 240, 230, 0.7);
  border-radius: 10px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  padding: 5px 0;
  font-size: 14px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid rgba(35, 43, 44, 0.1);
}

.order-total {
  font-size: 16px;
}

.total-amount {
  font-size: 20px;
  font-weight: bold;
  color: #b94d12;
  margin-left: 10px;
}

.order-actions {
  display: flex;
  gap: 10px;
}
</style>

