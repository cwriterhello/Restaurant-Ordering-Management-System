<template>
  <div class="admin-index">
    <el-container style="height: 100vh">
      <!-- 左侧菜单栏 -->
      <el-aside width="220px" class="admin-aside">
        <div class="logo-box">
          <h2>餐饮管理系统</h2>
        </div>
        <el-menu
            :default-active="activeMenu"
            class="admin-menu"
            @select="handleMenuSelect"
            background-color="#1e293b"
            text-color="#f1f5f9"
            active-text-color="#38bdf8"
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
            <h3>{{ currentTitle }}</h3>
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
import { useRouter } from 'vue-router'
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

const router = useRouter()
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
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* 左侧深色侧边栏（高级、护眼） */
.admin-aside {
  background: #1e293b;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
}

.logo-box {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #334155;
}

.logo-box h2 {
  margin: 0;
  color: #f1f5f9;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.admin-menu {
  border-right: none;
  height: calc(100vh - 64px);
  overflow-y: auto;
}

/* 顶部导航栏 */
.admin-header {
  background: #ffffff;
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
  color: #1e293b;
  font-size: 17px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-name {
  font-size: 14px;
  color: #475569;
  font-weight: 500;
}

/* 内容区域（柔和、卡片感） */
.admin-main {
  background: #f8fafc;
  padding: 0;
  overflow: auto;
}

.main-container {
  padding: 24px;
  min-height: calc(100vh - 64px);
}

/* 滚动条美化 */
:deep(.admin-menu::-webkit-scrollbar) {
  width: 4px;
}

:deep(.admin-menu::-webkit-scrollbar-thumb) {
  background: #475569;
  border-radius: 4px;
}
</style>