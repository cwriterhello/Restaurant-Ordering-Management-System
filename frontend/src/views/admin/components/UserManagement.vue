<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>
      
      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            {{ getRoleText(row.role) }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)" :disabled="row.username === 'admin'">
              删除
            </el-button>
            <el-button size="small" type="warning" @click="handleResetPassword(row)">
              重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑/新增用户对话框 -->
    <el-dialog
      :title="isEdit ? '编辑用户' : '新增用户'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" style="width: 100%" :disabled="userForm.username === 'admin'">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="收银员" value="CASHIER" />
            <el-option label="后厨" value="KITCHEN" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-else>
          <el-input v-model="userForm.password" type="password" placeholder="不修改密码请留空" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      title="重置密码"
      v-model="resetPasswordDialogVisible"
      width="400px"
    >
      <el-form :model="resetPasswordForm" :rules="resetPasswordRules" ref="resetPasswordFormRef" label-width="80px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPasswordForm.newPassword" type="password" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPasswordForm.confirmPassword" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleResetPasswordConfirm" :loading="resetting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getAllUsersApi, createUserApi, updateUserApi, deleteUserApi, resetUserPasswordApi } from '@/api/user'
import type { User } from '@/types/api'

const userStore = useUserStore()

// 用户列表相关
const users = ref<User[]>([])
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const userFormRef = ref()
const userForm = ref<User>({
  id: undefined,
  username: '',
  realName: '',
  role: 'CASHIER',
  phone: '',
  status: 1,
  password: ''
})

// 重置密码相关
const resetPasswordDialogVisible = ref(false)
const resetting = ref(false)
const resetPasswordFormRef = ref()
const resetPasswordForm = ref({
  newPassword: '',
  confirmPassword: ''
})
const targetUserId = ref<number>()

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== resetPasswordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getAllUsersApi()
    users.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (time: string | null) => {
  if (!time) return '--'
  return new Date(time).toLocaleString()
}

// 获取角色文本
const getRoleText = (role: string) => {
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'CASHIER': '收银员',
    'KITCHEN': '后厨'
  }
  return roleMap[role] || role
}

// 处理新增用户
const handleAdd = () => {
  isEdit.value = false
  userForm.value = {
    id: undefined,
    username: '',
    realName: '',
    role: 'CASHIER',
    phone: '',
    status: 1,
    password: ''
  }
  dialogVisible.value = true
}

// 处理编辑用户
const handleEdit = (user: User) => {
  isEdit.value = true
  // 克隆对象，避免直接修改原始数据
  userForm.value = { ...user }
  // 编辑时不显示原密码
  userForm.value.password = ''
  dialogVisible.value = true
}

// 处理保存用户
const handleSave = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    saving.value = true
    
    if (isEdit.value) {
      // 编辑用户
      await updateUserApi(userForm.value.id!, userForm.value)
      ElMessage.success('用户信息更新成功')
    } else {
      // 新增用户
      await createUserApi(userForm.value)
      ElMessage.success('用户创建成功')
    }
    
    dialogVisible.value = false
    await loadUsers() // 重新加载用户列表
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 处理删除用户
const handleDelete = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确认删除用户 ${user.username}？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteUserApi(user.id!)
    ElMessage.success('删除成功')
    await loadUsers() // 重新加载用户列表
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 处理重置密码（打开对话框）
const handleResetPassword = (user: User) => {
  targetUserId.value = user.id
  resetPasswordForm.value = {
    newPassword: '',
    confirmPassword: ''
  }
  resetPasswordDialogVisible.value = true
}

// 处理重置密码确认
const handleResetPasswordConfirm = async () => {
  if (!resetPasswordFormRef.value) return
  
  try {
    await resetPasswordFormRef.value.validate()
    resetting.value = true
    
    await resetUserPasswordApi(targetUserId.value!, {
      newPassword: resetPasswordForm.value.newPassword
    })
    
    ElMessage.success('密码重置成功')
    resetPasswordDialogVisible.value = false
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '密码重置失败')
    }
  } finally {
    resetting.value = false
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.dialog-footer {
  text-align: right;
}
</style>