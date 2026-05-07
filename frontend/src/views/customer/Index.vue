<template>
  <div class="app-page customer-index">
    <div class="app-shell">
      <header class="app-header">
        <div>
          <h1 class="app-title">扫码点餐</h1>
          <p class="app-subtitle">请扫描桌号二维码，或输入桌号快速进入点餐页</p>
        </div>
      </header>

      <main class="app-main">
        <el-card class="entry-card module-card">
          <div class="scan-area">
            <div class="scan-icon">
              <el-icon :size="70"><Camera /></el-icon>
            </div>
            <h2>开始下单</h2>
            <p>扫描桌号二维码</p>
            <el-input
              v-model="tableNumber"
              placeholder="或手动输入桌号，例如 A01"
              class="table-input"
              @keyup.enter="goToTable"
            >
              <template #append>
                <el-button @click="goToTable">确认</el-button>
              </template>
            </el-input>
          </div>
        </el-card>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { validateTableNumberApi } from '@/api/table'

const router = useRouter()
const tableNumber = ref('')

const goToTable = async () => {
  if (!tableNumber.value.trim()) {
    ElMessage.warning('请输入桌号')
    return
  }
  
  // 验证桌号是否存在
  try {
    const res = await validateTableNumberApi(tableNumber.value.trim())
    if (res.data) { // 桌号存在
      router.push(`/customer/table/${tableNumber.value.trim()}`)
    } else { // 桌号不存在
      ElMessage.error('桌号不存在，请检查桌号')
    }
  } catch (error) {
    ElMessage.error('验证桌号时发生错误')
  }
}
</script>

<style scoped>
.customer-index {
  display: flex;
  align-items: stretch;
}

.scan-area {
  min-height: 420px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  gap: 10px;
}

.entry-card {
  max-width: 760px;
  margin: 8px auto 0;
}

.scan-icon {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #f5f1eb;
  background: linear-gradient(135deg, #cd6e2d, #b94b12);
}

.scan-area h2 {
  margin-top: 10px;
  font-size: 32px;
}

.scan-area p {
  color: #6b787b;
}

.table-input {
  width: min(100%, 320px);
  margin-top: 8px;
}
</style>

