<template>
  <div class="order-list">
    <el-empty v-if="orders.length === 0" description="暂无待结账订单" />
    <div v-else class="orders-container">
      <el-card
        v-for="order in orders"
        :key="order.id"
        class="order-card"
      >
        <template #header>
          <div class="order-header">
            <span class="order-number">订单号：{{ order.orderNumber }}</span>
            <span class="order-time">{{ formatTime(order.createTime) }}</span>
          </div>
        </template>
        
        <div class="order-content">
          <div class="order-info">
            <p><strong>桌号：</strong>{{ order.tableNumber }}</p>
            <p v-if="order.memberName"><strong>会员：</strong>{{ order.memberName }} ({{ order.memberPhone }})</p>
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
          
          <div class="order-summary">
            <div class="summary-row">
              <span>订单总额：</span>
              <span>¥{{ order.totalAmount.toFixed(2) }}</span>
            </div>
            <div v-if="order.discountAmount > 0" class="summary-row">
              <span>折扣金额：</span>
              <span class="discount">-¥{{ order.discountAmount.toFixed(2) }}</span>
            </div>
            <div class="summary-row total">
              <span>实付金额：</span>
              <span class="total-amount">¥{{ order.actualAmount.toFixed(2) }}</span>
            </div>
          </div>
          
          <div class="order-actions">
            <el-button
              type="success"
              @click="handlePay(order.id, 'CASH')"
            >
              现金支付
            </el-button>
            <el-button
              type="primary"
              @click="handlePay(order.id, 'ALIPAY')"
            >
              支付宝
            </el-button>
            <el-button
              type="warning"
              @click="handlePay(order.id, 'WECHAT')"
            >
              微信支付
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import type { OrderVO } from '@/types/api'

const props = defineProps<{
  orders: OrderVO[]
}>()

const emit = defineEmits<{
  pay: [orderId: number, paymentMethod: string]
}>()

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

const handlePay = (orderId: number, paymentMethod: string) => {
  const methodMap: Record<string, string> = {
    'CASH': '现金',
    'ALIPAY': '支付宝',
    'WECHAT': '微信支付'
  }
  
  ElMessageBox.confirm(
    `确认使用${methodMap[paymentMethod]}支付？`,
    '确认支付',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    emit('pay', orderId, paymentMethod)
  }).catch(() => {})
}
</script>

<style scoped>
.order-list {
  padding: 20px;
}

.orders-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.order-card {
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
}

.order-time {
  font-size: 12px;
  color: #999;
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
  background: #f5f7fa;
  border-radius: 4px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  padding: 5px 0;
  font-size: 14px;
}

.order-summary {
  margin: 15px 0;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 5px 0;
  font-size: 14px;
}

.summary-row.total {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 2px solid #eee;
  font-size: 16px;
  font-weight: bold;
}

.discount {
  color: #67c23a;
}

.total-amount {
  color: #f56c6c;
  font-size: 20px;
}

.order-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.order-actions .el-button {
  flex: 1;
}
</style>

