<template>
  <div class="admin-index">
    <el-container>
      <el-header class="admin-header">
        <h1>管理员后台</h1>
        <div class="header-actions">
          <el-button type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      
      <el-main>
        <el-tabs v-model="activeTab">
          <el-tab-pane label="菜品管理" name="dishes">
            <DishManagement />
          </el-tab-pane>
          <el-tab-pane label="套餐管理" name="combos">
            <ComboManagement />
          </el-tab-pane>
          <el-tab-pane label="会员管理" name="members">
            <MemberManagement />
          </el-tab-pane>
          <el-tab-pane label="桌号二维码" name="tables">
            <TableQRCodes />
          </el-tab-pane>
          <el-tab-pane label="数据统计" name="statistics">
            <Statistics />
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import DishManagement from './components/DishManagement.vue'
import ComboManagement from './components/ComboManagement.vue'
import MemberManagement from './components/MemberManagement.vue'
import TableQRCodes from './components/TableQRCodes.vue'
import Statistics from './components/Statistics.vue'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('dishes')

const handleLogout = () => {
  userStore.logout()
}
</script>

<style scoped>
.admin-index {
  min-height: 100vh;
  background: #f5f7fa;
}

.admin-header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.admin-header h1 {
  margin: 0;
  color: #333;
}
</style>