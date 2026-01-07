<template>
  <div class="member-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>会员查询</span>
          <el-button type="primary" @click="showCreateDialog = true">新增会员</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" @submit.prevent="handleSearch">
        <el-form-item label="手机号">
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入手机号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
      
      <el-card v-if="member" class="member-info">
        <h3>会员信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="手机号">{{ member.phone }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ member.name || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="会员等级">{{ getLevelText(member.level) }}</el-descriptions-item>
          <el-descriptions-item label="折扣">{{ (member.discount * 10).toFixed(1) }}折</el-descriptions-item>
          <el-descriptions-item label="积分">{{ member.points }}</el-descriptions-item>
          <el-descriptions-item label="累计消费">¥{{ member.totalConsumption.toFixed(2) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-card>
    
    <!-- 创建会员对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="新增会员"
      width="400px"
    >
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="手机号" required>
          <el-input v-model="createForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="createForm.name" placeholder="请输入姓名（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getMemberByPhoneApi, createMemberApi } from '@/api/member'
import type { Member } from '@/types/api'

const searchForm = reactive({
  phone: ''
})

const member = ref<Member | null>(null)
const showCreateDialog = ref(false)
const creating = ref(false)

const createForm = reactive({
  phone: '',
  name: ''
})

const handleSearch = async () => {
  if (!searchForm.phone.trim()) {
    ElMessage.warning('请输入手机号')
    return
  }
  
  try {
    const res = await getMemberByPhoneApi(searchForm.phone)
    member.value = res.data
  } catch (error: any) {
    if (error.response?.data?.code === 404) {
      member.value = null
      ElMessage.info('该手机号未注册会员')
    } else {
      ElMessage.error('查询失败')
    }
  }
}

const handleCreate = async () => {
  if (!createForm.phone.trim()) {
    ElMessage.warning('请输入手机号')
    return
  }
  
  creating.value = true
  try {
    await createMemberApi(createForm.phone, createForm.name || undefined)
    ElMessage.success('会员创建成功')
    showCreateDialog.value = false
    createForm.phone = ''
    createForm.name = ''
    searchForm.phone = createForm.phone
    await handleSearch()
  } catch (error: any) {
    ElMessage.error(error.message || '创建失败')
  } finally {
    creating.value = false
  }
}

const getLevelText = (level: string) => {
  const levelMap: Record<string, string> = {
    'NORMAL': '普通会员',
    'SILVER': '银卡会员',
    'GOLD': '金卡会员',
    'PLATINUM': '白金会员'
  }
  return levelMap[level] || level
}
</script>

<style scoped>
.member-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.member-info {
  margin-top: 20px;
}

.member-info h3 {
  margin: 0 0 15px;
}
</style>

