<template>
  <div class="statistics">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>营业数据统计</span>
          </template>
          <el-form :inline="true" :model="searchForm">
            <el-form-item label="开始日期">
              <el-date-picker
                v-model="searchForm.startDate"
                type="date"
                placeholder="选择开始日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="结束日期">
              <el-date-picker
                v-model="searchForm.endDate"
                type="date"
                placeholder="选择结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadStatistics">查询</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="statistics" style="width: 100%">
            <el-table-column prop="statDate" label="日期" width="120" />
            <el-table-column prop="totalOrders" label="订单数" width="100" />
            <el-table-column prop="totalAmount" label="总营业额" width="120">
              <template #default="{ row }">
                ¥{{ row.totalAmount.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="cashAmount" label="现金" width="100">
              <template #default="{ row }">
                ¥{{ row.cashAmount.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="alipayAmount" label="支付宝" width="100">
              <template #default="{ row }">
                ¥{{ row.alipayAmount.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="wechatAmount" label="微信" width="100">
              <template #default="{ row }">
                ¥{{ row.wechatAmount.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="24" style="margin-top: 20px">
        <el-card>
          <template #header>
            <span>库存预警</span>
          </template>
          <el-table :data="stockAlerts" style="width: 100%">
            <el-table-column prop="dishName" label="菜品名称" />
            <el-table-column prop="currentStock" label="当前库存" width="100" />
            <el-table-column prop="alertThreshold" label="预警阈值" width="100" />
            <el-table-column prop="createTime" label="预警时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDailyStatisticsApi, getStockAlertsApi } from '@/api/statistics'
import type { DailyStatistics, StockAlert } from '@/types/api'
import { connectStockAlertWebSocket, disconnectWebSocket } from '@/utils/websocket'

const searchForm = reactive({
  startDate: '',
  endDate: ''
})

const statistics = ref<DailyStatistics[]>([])
const stockAlerts = ref<StockAlert[]>([])

const loadStatistics = async () => {
  try {
    const res = await getDailyStatisticsApi(searchForm.startDate, searchForm.endDate)
    statistics.value = res.data
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const loadStockAlerts = async () => {
  try {
    const res = await getStockAlertsApi()
    stockAlerts.value = res.data
  } catch (error) {
    ElMessage.error('加载库存预警失败')
  }
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadStatistics()
  loadStockAlerts()

  // 订阅实时库存预警
  connectStockAlertWebSocket((message: string) => {
    ElMessage.warning(message)
    // 每次收到预警后重新加载列表，保持和后端数据一致
    loadStockAlerts()
  })
})

onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
.statistics {
  padding: 20px;
}
</style>

