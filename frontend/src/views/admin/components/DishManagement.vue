<template>
  <div class="dish-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜品管理</span>
          <el-button type="primary" @click="handleAdd">新增菜品</el-button>
        </div>
      </template>
      
      <el-table :data="dishes" style="width: 100%">
        <el-table-column prop="name" label="菜品名称" width="150" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="isAvailable" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isAvailable === 1 ? 'success' : 'danger'">
              {{ row.isAvailable === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.isAvailable === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.isAvailable === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="dishForm" :rules="dishRules" ref="dishFormRef" label-width="100px">
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="dishForm.name" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="dishForm.categoryId" placeholder="请选择分类">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="dishForm.price" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="dishForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dishForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="是否推荐">
          <el-switch v-model="dishForm.isRecommend" :active-value="1" :inactive-value="0" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getDishesApi, getAllDishesApi, createDishApi, updateDishApi, updateDishStatusApi, deleteDishApi } from '@/api/dish'
import { getCategoriesApi } from '@/api/category'
import type { DishVO, DishDTO, Category } from '@/types/api'

const dishes = ref<DishVO[]>([])
const categories = ref<Category[]>([])
const showDialog = ref(false)
const saving = ref(false)
const dishFormRef = ref<FormInstance>()
const isEdit = ref(false)

const dishForm = reactive<DishDTO>({
  name: '',
  categoryId: 0,
  price: 0,
  stock: 0,
  description: '',
  isRecommend: 0
})

const dishRules = reactive<FormRules<DishDTO>>({
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
})

const dialogTitle = computed(() => isEdit.value ? '编辑菜品' : '新增菜品')

const loadDishes = async () => {
  try {
    // 使用新的API获取所有菜品（包括下架的）
    const res = await getAllDishesApi()
    dishes.value = res.data
  } catch (error) {
    ElMessage.error('加载菜品失败')
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoriesApi()
    categories.value = res.data
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(dishForm, {
    name: '',
    categoryId: 0,
    price: 0,
    stock: 0,
    description: '',
    isRecommend: 0
  })
  showDialog.value = true
}

const handleEdit = (dish: DishVO) => {
  isEdit.value = true
  Object.assign(dishForm, {
    id: dish.id,
    name: dish.name,
    categoryId: dish.categoryId,
    price: dish.price,
    stock: dish.stock,
    description: dish.description,
    isRecommend: dish.isRecommend
  })
  showDialog.value = true
}

const handleSave = async () => {
  if (!dishFormRef.value) return
  
  await dishFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        if (isEdit.value) {
          await updateDishApi(dishForm.id!, dishForm)
        } else {
          await createDishApi(dishForm)
        }
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        showDialog.value = false
        await loadDishes()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const handleToggleStatus = async (dish: DishVO) => {
  try {
    await updateDishStatusApi(dish.id, dish.isAvailable === 1 ? 0 : 1)
    ElMessage.success('状态更新成功')
    await loadDishes()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = async (dish: DishVO) => {
  try {
    await ElMessageBox.confirm(
      `确认删除菜品"${dish.name}"？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteDishApi(dish.id)
    ElMessage.success('删除成功')
    await loadDishes()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadDishes()
  loadCategories()
})
</script>


<style scoped>
.dish-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

