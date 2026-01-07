<template>
  <div class="combo-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>套餐管理</span>
          <el-button type="primary" @click="handleAdd">新增套餐</el-button>
        </div>
      </template>
      
      <el-table :data="combos" style="width: 100%">
        <el-table-column prop="name" label="套餐名称" width="150" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template #default="{ row }">
            <span v-if="row.originalPrice && row.originalPrice > 0">¥{{ row.originalPrice.toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="isAvailable" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isAvailable === 1 ? 'success' : 'danger'">
              {{ row.isAvailable === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleConfigureDishes(row)">配置菜品</el-button>
            <el-button
              size="small"
              :type="row.isAvailable === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.isAvailable === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑套餐对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑套餐' : '新增套餐'"
      width="500px"
    >
      <el-form :model="comboForm" :rules="comboRules" ref="comboFormRef" label-width="100px">
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="comboForm.name" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="comboForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入套餐描述"
          />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="comboForm.price"
            :precision="2"
            :min="0"
            placeholder="请输入价格"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number
            v-model="comboForm.originalPrice"
            :precision="2"
            :min="0"
            placeholder="请输入原价"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number
            v-model="comboForm.sortOrder"
            :min="0"
            placeholder="请输入排序"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="comboForm.isAvailable"
            :active-value="1"
            :inactive-value="0"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- 配置套餐菜品对话框 -->
    <el-dialog
      v-model="showDishConfigDialog"
      title="配置套餐菜品"
      width="800px"
    >
      <el-form label-width="100px">
        <el-form-item label="套餐名称">
          <el-input :value="currentCombo?.name" disabled />
        </el-form-item>
        <el-form-item label="选择菜品">
          <el-table
            :data="allDishes"
            @selection-change="handleDishSelectionChange"
            style="width: 100%"
          >
            <el-table-column type="selection" :selectable="isDishSelectable" />
            <el-table-column prop="name" label="菜品名称" width="150" />
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                ¥{{ row.price.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="categoryName" label="分类" width="100" />
            <el-table-column prop="stock" label="库存" width="80" />
            <el-table-column label="数量" width="120">
              <template #default="{ row }">
                <el-input-number
                  v-model="dishQuantities[row.id]"
                  :min="1"
                  :max="row.stock"
                  size="small"
                  @change="(value) => updateDishQuantity(row.id, value)"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDishConfigDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveDishConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { getCombosApi, createComboApi, updateComboApi, updateComboStatusApi, deleteComboApi, configureComboDishesApi } from '@/api/combo'
import { getDishesApi } from '@/api/dish'
import type { Combo, DishVO } from '@/types/api'

const combos = ref<Combo[]>([])
const allDishes = ref<DishVO[]>([])
const showDialog = ref(false)
const showDishConfigDialog = ref(false)
const comboFormRef = ref<FormInstance>()
const isEdit = ref(false)

const comboForm = reactive<Combo>({
  id: 0,
  name: '',
  description: '',
  image: '',
  price: 0,
  originalPrice: 0,
  isAvailable: 1,
  sortOrder: 0,
  createTime: '',
  updateTime: ''
})

const currentCombo = ref<Combo | null>(null)
const selectedDishes = ref<DishVO[]>([])
const dishQuantities = ref<Record<number, number>>({}) // 记录每个菜品的数量

const comboRules = reactive<FormRules<Combo>>({
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
})

const dialogTitle = computed(() => isEdit.value ? '编辑套餐' : '新增套餐')

const loadCombos = async () => {
  try {
    const res = await getCombosApi()
    combos.value = res.data
  } catch (error) {
    ElMessage.error('加载套餐失败')
  }
}

const loadDishes = async () => {
  try {
    const res = await getDishesApi()
    allDishes.value = res.data
  } catch (error) {
    ElMessage.error('加载菜品失败')
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(comboForm, {
    id: 0,
    name: '',
    description: '',
    image: '',
    price: 0,
    originalPrice: 0,
    isAvailable: 1,
    sortOrder: 0,
    createTime: '',
    updateTime: ''
  })
  showDialog.value = true
}

const handleEdit = (combo: Combo) => {
  isEdit.value = true
  Object.assign(comboForm, combo)
  showDialog.value = true
}

const handleConfigureDishes = (combo: Combo) => {
  currentCombo.value = combo
  // 初始化菜品数量
  dishQuantities.value = {}
  showDishConfigDialog.value = true
}

const isDishSelectable = (row: DishVO) => {
  // 只有上架且有库存的菜品才能被选择
  return row.isAvailable === 1 && row.stock > 0
}

const handleDishSelectionChange = (selection: DishVO[]) => {
  selectedDishes.value = selection
  // 为新选择的菜品初始化数量
  selection.forEach(dish => {
    if (dishQuantities.value[dish.id] === undefined) {
      dishQuantities.value[dish.id] = 1
    }
  })
}

const updateDishQuantity = (dishId: number, quantity: number) => {
  dishQuantities.value[dishId] = quantity
}

const handleToggleStatus = async (combo: Combo) => {
  try {
    await updateComboStatusApi(combo.id, combo.isAvailable === 1 ? 0 : 1)
    ElMessage.success('状态更新成功')
    await loadCombos()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = async (combo: Combo) => {
  try {
    await ElMessageBox.confirm(
      `确认删除套餐"${combo.name}"？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteComboApi(combo.id)
    ElMessage.success('删除成功')
    await loadCombos()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleSave = async () => {
  if (!comboFormRef.value) return
  
  try {
    await comboFormRef.value.validate()
    
    if (isEdit.value) {
      await updateComboApi(comboForm.id, comboForm)
      ElMessage.success('套餐更新成功')
    } else {
      await createComboApi(comboForm)
      ElMessage.success('套餐创建成功')
    }
    
    showDialog.value = false
    await loadCombos()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

const handleSaveDishConfig = async () => {
  if (!currentCombo.value) return
  
  if (selectedDishes.value.length === 0) {
    ElMessage.warning('请至少选择一个菜品')
    return
  }
  
  try {
    // 构建菜品配置对象
    const config = {
      comboId: currentCombo.value.id,
      dishes: selectedDishes.value.map(dish => ({
        dishId: dish.id,
        quantity: dishQuantities.value[dish.id] || 1
      }))
    }
    
    await configureComboDishesApi(currentCombo.value.id, config)
    ElMessage.success('套餐菜品配置成功')
    showDishConfigDialog.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '配置失败')
  }
}

onMounted(() => {
  loadCombos()
  loadDishes()
})
</script>

<style scoped>
.combo-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>