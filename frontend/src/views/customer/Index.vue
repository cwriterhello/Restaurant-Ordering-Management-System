<template>
  <div class="customer-index">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1>扫码点餐</h1>
          <p>请扫描桌号二维码开始点餐</p>
        </div>
      </el-header>
      
      <el-main>
        <el-card>
          <div class="scan-area">
            <el-icon :size="80" color="#409eff"><Camera /></el-icon>
            <p>扫描桌号二维码</p>
            <el-input
              v-model="tableNumber"
              placeholder="或手动输入桌号"
              style="width: 200px; margin-top: 20px"
              @keyup.enter="goToTable"
            >
              <template #append>
                <el-button @click="goToTable">确认</el-button>
              </template>
            </el-input>
          </div>
        </el-card>
      </el-main>
    </el-container>
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
  min-height: 100vh;
  background: #f5f7fa;
}

.header-content {
  text-align: center;
  padding: 20px 0;
}

.header-content h1 {
  margin: 0;
  color: #333;
}

.header-content p {
  margin: 10px 0 0;
  color: #666;
}

.scan-area {
  text-align: center;
  padding: 60px 20px;
}

.scan-area p {
  margin-top: 20px;
  color: #666;
  font-size: 16px;
}
</style>

