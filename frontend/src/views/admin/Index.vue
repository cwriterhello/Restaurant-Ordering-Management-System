<template>
  <div class="app-page admin-index">
    <el-container class="app-shell admin-shell">
      <!-- 左侧菜单栏 -->
      <el-aside width="220px" class="admin-aside">
        <div class="logo-box">
          <h2>餐饮管理系统</h2>
        </div>
        <el-menu
            :default-active="activeMenu"
            class="admin-menu"
            @select="handleMenuSelect"
            background-color="#1f2425"
            text-color="#f4efe7"
            active-text-color="#f1a066"
        >
          <el-menu-item index="kitchen">
            <el-icon><Monitor /></el-icon>
            <span>后厨管理</span>
          </el-menu-item>
          <el-menu-item index="cashier">
            <el-icon><Money /></el-icon>
            <span>收银管理</span>
          </el-menu-item>
          <el-sub-menu index="business">
            <template #title>
              <el-icon><Shop /></el-icon>
              <span>业务管理</span>
            </template>
            <el-menu-item index="dishes">菜品管理</el-menu-item>
            <el-menu-item index="combos">套餐管理</el-menu-item>
            <el-menu-item index="members">会员管理</el-menu-item>
            <el-menu-item index="tables">桌号管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="users">用户管理</el-menu-item>
            <el-menu-item index="statistics">数据统计</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区 -->
      <el-container>
        <el-header class="admin-header">
          <div class="header-left">
            <h3 class="app-title">{{ currentTitle }}</h3>
            <p class="app-subtitle">统一管理后厨、收银与业务数据</p>
          </div>
          <div class="header-right">
            <el-badge type="success" :value="getRoleText(userStore.role)" />
            <span class="user-name">{{ userStore.realName }}</span>
            <el-button type="danger" plain size="small" @click="handleLogout">
              <el-icon><Lock /></el-icon>退出登录
            </el-button>
          </div>
        </el-header>

        <el-main class="admin-main">
          <div class="main-container">
            <!-- 后厨管理 -->
            <KitchenView v-if="activeMenu === 'kitchen'" />

            <!-- 收银管理 -->
            <CashierView v-else-if="activeMenu === 'cashier'" />

            <!-- 菜品管理 -->
            <DishManagement v-else-if="activeMenu === 'dishes'" />

            <!-- 套餐管理 -->
            <ComboManagement v-else-if="activeMenu === 'combos'" />

            <!-- 会员管理 -->
            <MemberManagement v-else-if="activeMenu === 'members'" />

            <!-- 桌号管理 -->
            <TableQRCodes v-else-if="activeMenu === 'tables'" />

            <!-- 用户管理 -->
            <UserManagement v-else-if="activeMenu === 'users'" />

            <!-- 数据统计 -->
            <Statistics v-else-if="activeMenu === 'statistics'" />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { Monitor, Money, Shop, Setting, Lock } from '@element-plus/icons-vue'
import DishManagement from './components/DishManagement.vue'
import ComboManagement from './components/ComboManagement.vue'
import MemberManagement from './components/MemberManagement.vue'
import UserManagement from './components/UserManagement.vue'
import TableQRCodes from './components/TableQRCodes.vue'
import Statistics from './components/Statistics.vue'
import KitchenView from '../kitchen/Index.vue'
import CashierView from '../cashier/Index.vue'

const userStore = useUserStore()

const activeMenu = ref('kitchen')

const currentTitle = computed(() => {
  const titleMap: Record<string, string> = {
    kitchen: '后厨管理',
    cashier: '收银管理',
    dishes: '菜品管理',
    combos: '套餐管理',
    members: '会员管理',
    tables: '桌号管理',
    users: '用户管理',
    statistics: '数据统计'
  }
  return titleMap[activeMenu.value] || '管理系统'
})

const handleMenuSelect = (index: string) => {
  activeMenu.value = index
}

const getRoleText = (role: string) => {
  const roleMap: Record<string, string> = {
    ADMIN: '管理员',
    CASHIER: '收银员',
    KITCHEN: '后厨'
  }
  return roleMap[role] || role
}

const handleLogout = () => {
  userStore.logout()
}
</script>

<style scoped>
/* 整体布局 */
.admin-index {
  display: flex;
  align-items: stretch;
}

.admin-shell {
  width: 100%;
  min-height: calc(100vh - clamp(28px, 4vw, 48px));
}

.admin-aside {
  background: #1f2425;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
}

.logo-box {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.logo-box h2 {
  margin: 0;
  color: #f7efe2;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.admin-menu {
  border-right: none;
  height: calc(100vh - 64px);
  overflow-y: auto;
}

.admin-header {
  background: linear-gradient(120deg, rgba(255, 251, 246, 0.88), rgba(248, 239, 226, 0.84));
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 10;
}

.header-left h3 {
  margin: 0;
  font-size: clamp(24px, 2.2vw, 32px);
}

.header-left .app-subtitle {
  margin-top: 4px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-name {
  font-size: 14px;
  color: #47575b;
  font-weight: 500;
}

.admin-main {
  background: transparent;
  padding: 0;
  overflow: auto;
}

.main-container {
  padding: 24px;
  min-height: calc(100vh - 64px);
}

:deep(.admin-menu::-webkit-scrollbar) {
  width: 4px;
}

:deep(.admin-menu::-webkit-scrollbar-thumb) {
  background: rgba(250, 230, 207, 0.38);
  border-radius: 4px;
}
</style>
