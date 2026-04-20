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
            <el-button size="small" @click="handleViewDetail(row)">查看详情</el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
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
      width="800px"
      @close="handleCancel"
    >
    
    <!-- 套餐详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="套餐详情"
      width="800px"
    >
      <div class="detail-content">
        <div class="detail-row">
          <span class="label">套餐名称：</span>
          <span class="value">{{ currentCombo?.name }}</span>
        </div>
        <div class="detail-row">
          <span class="label">描述：</span>
          <span class="value">{{ currentCombo?.description || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">价格：</span>
          <span class="value">¥{{ currentCombo?.price.toFixed(2) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">原价：</span>
          <span class="value" v-if="currentCombo?.originalPrice && currentCombo.originalPrice > 0">¥{{ currentCombo.originalPrice.toFixed(2) }}</span>
          <span class="value" v-else>-</span>
        </div>
        <div class="detail-row">
          <span class="label">状态：</span>
          <span class="value">
            <el-tag :type="currentCombo?.isAvailable === 1 ? 'success' : 'danger'">
              {{ currentCombo?.isAvailable === 1 ? '上架' : '下架' }}
            </el-tag>
          </span>
        </div>
        <div class="detail-row">
          <span class="label">排序：</span>
          <span class="value">{{ currentCombo?.sortOrder }}</span>
        </div>
        <div class="detail-row">
          <span class="label">创建时间：</span>
          <span class="value">{{ currentCombo?.createTime }}</span>
        </div>
        <div class="detail-row">
          <span class="label">更新时间：</span>
          <span class="value">{{ currentCombo?.updateTime }}</span>
        </div>
        
        <h4 style="margin: 20px 0 10px;">包含菜品：</h4>
        <el-table :data="currentCombo?.dishes || []" style="width: 100%" v-if="showDetailDialog" :key="currentCombo?.id">
          <el-table-column prop="dishName" label="菜品名称" />
          <el-table-column prop="quantity" label="数量" />
          <el-table-column label="单价">
            <template #default="{ row }">
              ¥{{ getDishPrice(row.dishId).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="小计">
            <template #default="{ row }">
              ¥{{ (getDishPrice(row.dishId) * row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
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
        <el-form-item label="包含菜品">
          <el-table
            ref="dishTableRef"
            :data="allDishes"
            @selection-change="handleDishSelectionChange"
            style="width: 100%"
          >
            <el-table-column type="selection" :selectable="isDishSelectable" />
            <el-table-column prop="name" label="菜品名称" width="150" />
            <el-table-column prop="categoryName" label="分类" width="100" />
            <el-table-column prop="stock" label="库存" width="80" />
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                ¥{{ row.price.toFixed(2) }}
              </template>
            </el-table-column>
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
            <el-table-column label="小计" width="100">
              <template #default="{ row }">
                ¥{{ (row.price * (dishQuantities[row.id] || 1)).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>


  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { getCombosApi, getComboByIdApi, createComboApi, updateComboApi, updateComboStatusApi, deleteComboApi, configureComboDishesApi, getCombosWithDishesApi } from '@/api/combo'
import { getDishesApi } from '@/api/dish'
import type { Combo, ComboVO, DishVO } from '@/types/api'

const combos = ref<Combo[]>([])
const allDishes = ref<DishVO[]>([])
const showDialog = ref(false)
const showDetailDialog = ref(false)
const currentCombo = ref<ComboVO | null>(null)
const comboFormRef = ref<FormInstance>()
const dishTableRef = ref()
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



const isDishSelectable = (row: DishVO) => {
  // 只有上架且有库存的菜品才能被选择
  return row.isAvailable === 1 && row.stock > 0
}

const handleDishSelectionChange = (selection: DishVO[]) => {
  // 记录当前选中的菜品ID
  const currentIds = new Set(selectedDishes.value.map(dish => dish.id));
  // 记录新的选中菜品ID
  const newIds = new Set(selection.map(dish => dish.id));
    
  // 更新选中状态
  selectedDishes.value = selection;
    
  // 处理新增的菜品：初始化数量为1
  selection.forEach(dish => {
    if (dishQuantities.value[dish.id] === undefined) {
      dishQuantities.value[dish.id] = 1;
    }
  });
    
  // 处理取消选择的菜品：重置其数量
  currentIds.forEach(id => {
    if (!newIds.has(id)) {
      // 如果该菜品被取消选择，则从数量记录中删除
      delete dishQuantities.value[id];
    }
  });
    
  // 更新套餐价格
  updateComboPrice();
}

const updateDishQuantity = (dishId: number, quantity: number) => {
  if (quantity > 0) {
    dishQuantities.value[dishId] = quantity;
  }
  // 更新套餐价格
  updateComboPrice();
}

// 计算所选菜品的总价格
const calculateTotalPrice = (): number => {
  return selectedDishes.value.reduce((total, dish) => {
    const quantity = dishQuantities.value[dish.id] || 1;
    return total + (dish.price * quantity);
  }, 0);
}

// 更新套餐价格
const updateComboPrice = () => {
  if (selectedDishes.value.length > 0) {
    const totalPrice = calculateTotalPrice();
    // 价格和原价保持一致
    comboForm.price = parseFloat(totalPrice.toFixed(2));
    comboForm.originalPrice = parseFloat(totalPrice.toFixed(2));
  } else {
    // 如果没有选择菜品，清空价格
    comboForm.price = 0;
    comboForm.originalPrice = 0;
  }
};

// 监听所选菜品的变化
watch(selectedDishes, () => {
  updateComboPrice();
});

// 监听菜品数量的变化
watch(() => Object.values(dishQuantities.value), () => {
  updateComboPrice();
}, { deep: true });

const handleToggleStatus = async (combo: Combo) => {
  try {
    const res = await updateComboStatusApi(combo.id, combo.isAvailable === 1 ? 0 : 1)
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
      await loadCombos()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
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
    const res = await deleteComboApi(combo.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadCombos()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 查看套餐详情
const handleViewDetail = async (combo: Combo) => {
  try {
    // 获取完整的套餐信息，包括包含的菜品
    try {
      // 直接使用getComboByIdApi获取套餐详情，因为后端的GET /api/combos/{id}已经返回完整信息
      const res = await getComboByIdApi(combo.id)
      if (res.code === 200) {
        // 使用Vue的reactive API创建新的响应式对象
        currentCombo.value = {
          ...res.data,
          dishes: res.data.dishes ? [...res.data.dishes] : []
        };
        // 触发对话框显示
        showDetailDialog.value = true;
        // 确保对话框内容更新
        await nextTick();
      } else {
        ElMessage.error(res.message || '获取套餐详情失败')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '获取套餐详情失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取套餐详情失败')
  }
}

// 根据dishId获取菜品价格
const getDishPrice = (dishId: number | string | bigint): number => {
  // 如果没有加载套餐数据，返回0
  if (!currentCombo.value || !currentCombo.value.dishes) {
    return 0;
  }
  // 在套餐详情的菜品列表中查找对应菜品
  const dish = currentCombo.value.dishes.find(d => Number(d.dishId) === Number(dishId));
  if (!dish || dish.price == null) {
    return 0;
  }
  // 处理不同类型的price值
  if (typeof dish.price === 'number') {
    return dish.price;
  } else if (typeof dish.price === 'string') {
    return parseFloat(dish.price);
  } else if (dish.price && typeof dish.price === 'object' && 'val' in dish.price) {
    // 处理BigDecimal对象
    return parseFloat(String(dish.price.val));
  }
  return 0;
}

// 取消操作，重置表单和选择状态
const handleCancel = () => {
  // 关闭对话框
  showDialog.value = false;
  // 重置表单
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
  });
  // 重置菜品选择
  selectedDishes.value = [];
  // 重置数量记录
  Object.keys(dishQuantities.value).forEach(key => {
    delete dishQuantities.value[key];
  });
  // 清除表格选择状态
  if (dishTableRef.value) {
    dishTableRef.value.clearSelection();
  }
  // 更新价格（会清空）
  updateComboPrice();
};

const handleSave = async () => {
  if (!comboFormRef.value) return
      
  try {
    await comboFormRef.value.validate()
            
    // 确保价格不为空
    const finalPrice = comboForm.price || 0;
    const finalOriginalPrice = comboForm.originalPrice || 0;
        
    let res;
    if (isEdit.value) {
      res = await updateComboApi(comboForm.id, {
        ...comboForm,
        price: finalPrice,
        originalPrice: finalOriginalPrice
      })
              
      // 如果有选择菜品，配置套餐菜品
      if (selectedDishes.value.length > 0) {
        const config = {
          comboId: res.data.id,
          dishes: selectedDishes.value.map(dish => ({
            dishId: dish.id,
            quantity: dishQuantities.value[dish.id] || 1
          }))
        }
                
        // 使用新的接口进行套餐菜品配置
        const configRes = await configureComboDishesApi(String(res.data.id), config);
        if (configRes.code !== 200) {
          ElMessage.warning('套餐更新成功，但菜品配置失败：' + configRes.message);
        }
      }
    } else {
      // 创建套餐并配置菜品
      const comboData = {
        combo: { 
          ...comboForm,
          price: finalPrice,
          originalPrice: finalOriginalPrice
        },
        dishesConfig: {
          dishes: selectedDishes.value.map(dish => ({
            dishId: dish.id,
            quantity: dishQuantities.value[dish.id] || 1
          }))
        }
      }
              
      res = await createComboApi(comboData)
    }
        
    if (res.code === 200) {
      ElMessage.success(res.message || (isEdit.value ? '套餐更新成功' : '套餐创建成功'))
      // 成功后重置表单
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
      });
      // 重置菜品选择
      selectedDishes.value = [];
      // 重置数量记录
      Object.keys(dishQuantities.value).forEach(key => {
        delete dishQuantities.value[key];
      });
      // 清除表格选择状态
      if (dishTableRef.value) {
        dishTableRef.value.clearSelection();
      }
      // 更新价格（会清空）
      updateComboPrice();
            
      showDialog.value = false
      // 确保数据完全同步
      await new Promise(resolve => setTimeout(resolve, 500));
      await loadCombos()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
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

.detail-content {
  padding: 10px 20px;
}

.detail-row {
  display: flex;
  margin-bottom: 15px;
}

.label {
  width: 100px;
  font-weight: bold;
  color: #666;
}

.value {
  flex: 1;
  color: #333;
}


</style>