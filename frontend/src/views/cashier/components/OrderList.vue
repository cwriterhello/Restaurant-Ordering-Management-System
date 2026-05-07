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
              :loading="submittingOrderId === order.id && submittingMethod === 'CASH'"
              @click="handlePay(order.id, 'CASH')"
            >
              现金支付
            </el-button>
            <el-button
              type="primary"
              :loading="submittingOrderId === order.id && submittingMethod === 'ALIPAY'"
              @click="handlePay(order.id, 'ALIPAY')"
            >
              支付宝
            </el-button>
            <el-button
              type="warning"
              :loading="submittingOrderId === order.id && submittingMethod === 'WECHAT'"
              @click="handlePay(order.id, 'WECHAT')"
            >
              微信支付
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog
      v-model="onlinePayVisible"
      :title="`${methodMap[selectedMethod]}收款`"
      width="420px"
      destroy-on-close
      @close="closeOnlinePaymentDialog"
    >
      <div v-if="selectedOrder" class="online-pay-content">
        <div class="pay-header">
          <p>订单号：{{ selectedOrder.orderNumber }}</p>
          <p>桌号：{{ selectedOrder.tableNumber }}</p>
          <p class="pay-amount">应收金额：¥{{ selectedOrder.actualAmount.toFixed(2) }}</p>
          <p>支付单号：{{ onlinePaymentNo }}</p>
          <p>有效期至：{{ onlinePaymentExpireText }}</p>
        </div>

        <div class="qr-box">
          <qrcode-vue :value="qrCodeValue" :size="180" level="M" />
        </div>

        <p class="pay-hint">
          请使用{{ methodMap[selectedMethod] }}扫码付款，顾客完成付款后点击“确认已支付”。
        </p>
      </div>
      <template #footer>
        <el-button @click="closeOnlinePaymentDialog">取消</el-button>
        <el-button
          type="primary"
          :loading="confirmingOnlinePay"
          @click="confirmOnlinePay"
        >
          确认已支付
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import QrcodeVue from 'qrcode.vue'
import type { OrderVO } from '@/types/api'
import { confirmOnlinePaymentApi, createOnlinePaymentApi, getOnlinePaymentStatusApi } from '@/api/order'

type PaymentMethod = 'CASH' | 'ALIPAY' | 'WECHAT'

const props = defineProps<{
  orders: OrderVO[]
  cashierId?: number
}>()

const emit = defineEmits<{
  pay: [orderId: number, paymentMethod: PaymentMethod]
  paid: []
}>()

const onlinePayVisible = ref(false)
const selectedMethod = ref<PaymentMethod>('ALIPAY')
const selectedOrder = ref<OrderVO | null>(null)
const confirmingOnlinePay = ref(false)
const submittingOrderId = ref<number | null>(null)
const submittingMethod = ref<PaymentMethod | null>(null)
const onlinePaymentNo = ref('')
const onlinePaymentExpireTime = ref('')
const qrCodeValue = ref('')
const statusPollingTimer = ref<number | null>(null)

const methodMap: Record<PaymentMethod, string> = {
  CASH: '现金',
  ALIPAY: '支付宝',
  WECHAT: '微信支付'
}

const onlinePaymentExpireText = computed(() =>
  onlinePaymentExpireTime.value ? formatTime(onlinePaymentExpireTime.value) : '--'
)

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

const handlePay = async (orderId: number, paymentMethod: PaymentMethod) => {
  if (paymentMethod === 'CASH') {
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
    return
  }

  const order = props.orders.find(item => item.id === orderId)
  if (!order) {
    ElMessage.error('订单不存在或已更新，请刷新后重试')
    return
  }
  submittingOrderId.value = orderId
  submittingMethod.value = paymentMethod
  try {
    const res = await createOnlinePaymentApi(orderId, {
      paymentMethod,
      cashierId: props.cashierId
    })
    selectedMethod.value = paymentMethod
    selectedOrder.value = order
    onlinePaymentNo.value = res.data.paymentNo
    onlinePaymentExpireTime.value = res.data.expireTime
    qrCodeValue.value = res.data.qrCodeContent
    onlinePayVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '创建在线支付单失败')
  } finally {
    submittingOrderId.value = null
    submittingMethod.value = null
  }
}

const confirmOnlinePay = async () => {
  if (!selectedOrder.value || !onlinePaymentNo.value) {
    return
  }
  confirmingOnlinePay.value = true
  try {
    await confirmOnlinePaymentApi(selectedOrder.value.id, onlinePaymentNo.value, {
      cashierId: props.cashierId
    })
    ElMessage.success('支付确认成功')
    closeOnlinePaymentDialog()
    emit('paid')
  } catch (error: any) {
    ElMessage.error(error.message || '在线支付确认失败')
  } finally {
    confirmingOnlinePay.value = false
  }
}

const checkOnlinePaymentStatus = async () => {
  if (!selectedOrder.value || !onlinePaymentNo.value) {
    return
  }
  try {
    const res = await getOnlinePaymentStatusApi(selectedOrder.value.id, onlinePaymentNo.value)
    if (res.data.paymentStatus === 'SUCCESS') {
      ElMessage.success('系统检测到支付已完成')
      closeOnlinePaymentDialog()
      emit('paid')
    } else if (res.data.paymentStatus === 'EXPIRED') {
      ElMessage.warning('支付单已过期，请重新发起支付')
      closeOnlinePaymentDialog()
    }
  } catch (error) {
    // 轮询接口失败时保持静默，避免频繁打断收银操作
  }
}

const startStatusPolling = () => {
  stopStatusPolling()
  statusPollingTimer.value = window.setInterval(() => {
    checkOnlinePaymentStatus()
  }, 3000)
}

const stopStatusPolling = () => {
  if (statusPollingTimer.value !== null) {
    window.clearInterval(statusPollingTimer.value)
    statusPollingTimer.value = null
  }
}

const closeOnlinePaymentDialog = () => {
  onlinePayVisible.value = false
  selectedOrder.value = null
  onlinePaymentNo.value = ''
  onlinePaymentExpireTime.value = ''
  qrCodeValue.value = ''
  stopStatusPolling()
}

watch(onlinePayVisible, (visible) => {
  if (visible) {
    startStatusPolling()
  } else {
    stopStatusPolling()
  }
})

onBeforeUnmount(() => {
  stopStatusPolling()
})
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

.order-summary {
  margin: 15px 0;
  padding: 15px;
  background: rgba(247, 240, 230, 0.55);
  border-radius: 10px;
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
  border-top: 1px solid rgba(35, 43, 44, 0.12);
  font-size: 16px;
  font-weight: bold;
}

.discount {
  color: #67c23a;
}

.total-amount {
  color: #b94d12;
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

.online-pay-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.pay-header p {
  margin: 0 0 6px;
  color: #5f6d71;
}

.pay-amount {
  font-size: 18px;
  font-weight: 700;
  color: #b94d12 !important;
}

.qr-box {
  display: flex;
  justify-content: center;
  padding: 14px;
  background: rgba(247, 240, 230, 0.7);
  border-radius: 12px;
}

.pay-hint {
  margin: 0;
  color: #6b797d;
  line-height: 1.6;
  font-size: 13px;
}
</style>

