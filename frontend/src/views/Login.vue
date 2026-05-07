<template>
  <div class="login-page">
    <section class="brand-panel">
      <p class="panel-kicker">Bistro Operating Console</p>
      <h1>餐饮门店点餐管理系统</h1>
      <p class="panel-desc">
        从后厨到收银，一个界面掌控菜品、订单与运营节奏。
      </p>

      <div class="panel-tags">
        <span>实时看板</span>
        <span>多角色协同</span>
        <span>稳定高效</span>
      </div>
    </section>

    <section class="form-panel">
      <el-card class="login-card">
        <template #header>
          <div class="card-header">
            <h2>欢迎回来</h2>
            <p>请输入账号信息以继续工作</p>
          </div>
        </template>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="submit-btn"
              @click="handleLogin"
            >
              登录系统
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-tips">
          <h3>测试账号</h3>
          <p>管理员：admin / admin123</p>
          <p>收银员：cashier1 / admin123</p>
          <p>后厨：kitchen1 / admin123</p>
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { LoginRequest } from '@/types/api'

const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive<LoginRequest>({
  username: '',
  password: ''
})

const loginRules = reactive<FormRules<LoginRequest>>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
})

const handleLogin = async () => {
  if (!loginFormRef.value) {
    return
  }

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }

    loading.value = true
    try {
      await userStore.login(loginForm)
      ElMessage.success('登录成功')
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(360px, 1.1fr) minmax(340px, 0.9fr);
  gap: 28px;
  padding: clamp(20px, 4vw, 48px);
  position: relative;
  overflow: hidden;
}

.login-page::before,
.login-page::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  filter: blur(6px);
  pointer-events: none;
}

.login-page::before {
  width: 300px;
  height: 300px;
  top: -110px;
  right: 20%;
  background: radial-gradient(circle, rgba(233, 146, 70, 0.56), rgba(233, 146, 70, 0));
}

.login-page::after {
  width: 420px;
  height: 420px;
  bottom: -180px;
  left: -90px;
  background: radial-gradient(circle, rgba(54, 98, 93, 0.38), rgba(54, 98, 93, 0));
}

.brand-panel,
.form-panel {
  position: relative;
  z-index: 1;
}

.brand-panel {
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  padding: clamp(28px, 5vw, 64px);
  color: #f5f2ed;
  background:
    linear-gradient(160deg, rgba(22, 23, 24, 0.9), rgba(31, 33, 36, 0.84)),
    radial-gradient(circle at 78% 16%, rgba(219, 113, 43, 0.32), transparent 38%);
  box-shadow: 0 22px 52px rgba(12, 16, 18, 0.35);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.panel-kicker {
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: rgba(248, 216, 186, 0.88);
  margin-bottom: 16px;
}

.brand-panel h1 {
  font-size: clamp(34px, 4.3vw, 58px);
  line-height: 1.1;
  margin-bottom: 20px;
}

.panel-desc {
  max-width: 32ch;
  color: rgba(239, 233, 227, 0.86);
  line-height: 1.75;
  margin-bottom: 26px;
}

.panel-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.panel-tags span {
  border: 1px solid rgba(245, 219, 194, 0.4);
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 13px;
  background: rgba(247, 232, 216, 0.1);
}

.form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: min(100%, 460px);
  border-radius: 22px;
}

.card-header h2 {
  font-size: 32px;
  line-height: 1.05;
  margin-bottom: 8px;
  color: #1f2628;
}

.card-header p {
  color: #667477;
  font-size: 14px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 6px;
  border-radius: 12px;
  background: linear-gradient(110deg, #cf6e2b 0%, #b84f17 100%);
  border: none;
}

.submit-btn:hover {
  filter: brightness(1.05);
}

.login-tips {
  margin-top: 16px;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px dashed rgba(31, 38, 40, 0.18);
  background: rgba(247, 241, 230, 0.7);
}

.login-tips h3 {
  font-size: 14px;
  margin-bottom: 6px;
}

.login-tips p {
  font-size: 13px;
  color: #5d6b6e;
  line-height: 1.65;
}

@media (max-width: 920px) {
  .login-page {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .brand-panel {
    padding: 28px 24px;
  }

  .brand-panel h1 {
    font-size: clamp(30px, 8vw, 44px);
  }

  .panel-desc {
    max-width: none;
  }
}
</style>
