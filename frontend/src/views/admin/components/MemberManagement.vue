<template>
  <div class="member-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>会员管理</span>
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
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="filteredMembers" style="width: 100%" v-loading="loading">
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="level" label="会员等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discount" label="折扣" width="100">
          <template #default="{ row }">
            {{ (row.discount * 10).toFixed(1) }}折
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column prop="totalConsumption" label="累计消费" width="120">
          <template #default="{ row }">
            ¥{{ row.totalConsumption.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 编辑会员对话框 -->
    <el-dialog
      v-model="showDialog"
      title="编辑会员"
      width="500px"
    >
      <el-form :model="memberForm" :rules="memberRules" ref="memberFormRef" label-width="100px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="memberForm.phone" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="memberForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="会员等级" prop="level">
          <el-select v-model="memberForm.level" placeholder="请选择会员等级" style="width: 100%">
            <el-option label="普通会员" value="NORMAL" />
            <el-option label="银卡会员" value="SILVER" />
            <el-option label="金卡会员" value="GOLD" />
            <el-option label="白金会员" value="PLATINUM" />
          </el-select>
        </el-form-item>
        <el-form-item label="折扣" prop="discount">
          <el-input-number
            v-model="memberForm.discount"
            :min="0.1"
            :max="1.0"
            :step="0.1"
            :precision="2"
            placeholder="请输入折扣"
            style="width: 100%"
          />
          <div style="font-size: 12px; color: #999; margin-top: 5px;">
            例如：0.9 代表 9 折，1.0 代表原价
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getMembersApi, updateMemberApi } from '@/api/member'
import type { Member } from '@/types/api'

const allMembers = ref<Member[]>([])
const loading = ref(false)

const searchForm = reactive({
  phone: ''
})

const showDialog = ref(false)
const saving = ref(false)
const memberFormRef = ref<FormInstance>()

const memberForm = reactive<Partial<Member>>({
  id: 0,
  phone: '',
  name: '',
  level: '',
  discount: 1.0
})

const memberRules = reactive<FormRules<Partial<Member>>>({
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  level: [{ required: true, message: '请选择会员等级', trigger: 'change' }],
  discount: [{ required: true, message: '请输入折扣', trigger: 'blur' }]
})

const filteredMembers = computed(() => {
  if (!searchForm.phone) {
    return allMembers.value
  }
  return allMembers.value.filter(member => 
    member.phone.includes(searchForm.phone)
  )
})

const loadMembers = async () => {
  loading.value = true
  try {
    const res = await getMembersApi()
    allMembers.value = res.data
  } catch (error) {
    ElMessage.error('加载会员失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 过滤逻辑在computed中实现
}

const resetSearch = () => {
  searchForm.phone = ''
}

const handleEdit = (member: Member) => {
  Object.assign(memberForm, member)
  memberForm.discount = Number(member.discount)
  showDialog.value = true
}

const handleSave = async () => {
  if (!memberFormRef.value) return
  
  try {
    await memberFormRef.value.validate()
    saving.value = true
    
    await updateMemberApi(memberForm.id!, {
      name: memberForm.name,
      level: memberForm.level,
      discount: memberForm.discount ? memberForm.discount.toString() : undefined
    })
    
    ElMessage.success('会员信息更新成功')
    showDialog.value = false
    await loadMembers() // 重新加载会员列表
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    saving.value = false
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

const getLevelType = (level: string) => {
  const typeMap: Record<string, string> = {
    'NORMAL': 'info',
    'SILVER': 'primary',
    'GOLD': 'warning',
    'PLATINUM': 'danger'
  }
  return typeMap[level] || 'info'
}

onMounted(() => {
  loadMembers()
})
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
</style>