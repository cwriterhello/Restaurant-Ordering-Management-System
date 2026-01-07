<template>
  <div class="table-qrs">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>桌号二维码</span>
          <el-button type="primary" @click="loadTables" :loading="loading">
            刷新
          </el-button>
        </div>
      </template>

      <el-empty v-if="tables.length === 0" description="暂无桌号数据" />

      <div v-else class="qr-grid">
        <el-card
          v-for="table in tables"
          :key="table.id"
          class="qr-card"
        >
          <div class="qr-header">
            <span class="table-number">桌号：{{ table.tableNumber }}</span>
            <el-tag size="small" :type="getStatusType(table.status)">
              {{ getStatusText(table.status) }}
            </el-tag>
          </div>
          <qrcode-vue
            :value="buildTableUrl(table.tableNumber)"
            :size="140"
            level="M"
          />
          <div class="qr-footer">
            <p class="url">{{ buildTableUrl(table.tableNumber) }}</p>
            <el-button size="small" @click="copyUrl(table.tableNumber)">
              复制链接
            </el-button>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import QrcodeVue from 'qrcode.vue'
import { getTablesApi } from '@/api/table'
import type { Table } from '@/types/api'

const tables = ref<Table[]>([])
const loading = ref(false)

const loadTables = async () => {
  loading.value = true
  try {
    const res = await getTablesApi()
    tables.value = res.data
  } catch (error) {
    ElMessage.error('加载桌号失败')
  } finally {
    loading.value = false
  }
}

const buildTableUrl = (tableNumber: string) => {
  const { origin } = window.location
  return `${origin}/customer/table/${encodeURIComponent(tableNumber)}`
}

const copyUrl = async (tableNumber: string) => {
  const url = buildTableUrl(tableNumber)
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('链接已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动选择复制')
  }
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    EMPTY: '空闲',
    OCCUPIED: '用餐中',
    DISABLED: '停用'
  }
  return map[status] || status
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    EMPTY: 'success',
    OCCUPIED: 'warning',
    DISABLED: 'danger'
  }
  return map[status] || 'info'
}

onMounted(() => {
  loadTables()
})
</script>

<style scoped>
.table-qrs {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.qr-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 10px;
}

.qr-card {
  text-align: center;
}

.qr-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.table-number {
  font-weight: bold;
}

.qr-footer {
  margin-top: 10px;
}

.url {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
  margin-bottom: 6px;
}
</style>


