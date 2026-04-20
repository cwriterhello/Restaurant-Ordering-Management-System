<template>
  <div class="table-order">
    <el-container>
      <el-header class="order-header">
        <div class="header-left">
          <h2>桌号：{{ tableNumber }}</h2>
        </div>
        <div class="header-right">
          <el-badge :value="cartStore.getTotalCount()" :hidden="cartStore.getTotalCount() === 0">
            <el-button type="primary" @click="showCart = true">
              <el-icon><ShoppingCart /></el-icon>
              购物车
            </el-button>
          </el-badge>
        </div>
      </el-header>
      
      <el-main v-if="tableValidated">
        <!-- 当前订单状态显示 -->
        <div v-if="currentOrderId !== null" class="order-status">
          <el-alert
            title="订单处理中"
            type="info"
            description="您的订单正在处理中，请等待服务员上菜。"
            :closable="false"
            show-icon
          />
        </div>
        
        <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
          <!-- 所有菜品标签页 -->
          <el-tab-pane label="所有菜品" name="all">
            <div class="dish-grid">
              <el-card
                v-for="dish in allDishes"
                :key="dish.id"
                class="dish-card"
                :class="{ 'out-of-stock': dish.stock === 0 || dish.isAvailable === 0 }"
                @click="handleDishClick(dish)"
              >
                <div class="dish-image">
                  <el-image
                    :src="dish.image || '/placeholder.png'"
                    fit="cover"
                    style="width: 100%; height: 150px"
                  >
                    <template #error>
                      <div class="image-slot">暂无图片</div>
                    </template>
                  </el-image>
                  <el-tag v-if="dish.isRecommend === 1" type="danger" class="recommend-tag">推荐</el-tag>
                </div>
                <div class="dish-info">
                  <h3>{{ dish.name }}</h3>
                  <p class="dish-desc">{{ dish.description }}</p>
                  <div class="dish-footer">
                    <span class="dish-price">¥{{ dish.price }}</span>
                    <el-button
                      type="primary"
                      size="small"
                      :disabled="dish.stock === 0 || dish.isAvailable === 0"
                      @click.stop="addToCart(dish)"
                    >
                      加入购物车
                    </el-button>
                  </div>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
          <el-tab-pane
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :name="category.id.toString()"
          >
            <div class="dish-grid">
              <el-card
                v-for="dish in dishes"
                :key="dish.id"
                class="dish-card"
                :class="{ 'out-of-stock': dish.stock === 0 || dish.isAvailable === 0 }"
                @click="handleDishClick(dish)"
              >
                <div class="dish-image">
                  <el-image
                    :src="dish.image || '/placeholder.png'"
                    fit="cover"
                    style="width: 100%; height: 150px"
                  >
                    <template #error>
                      <div class="image-slot">暂无图片</div>
                    </template>
                  </el-image>
                  <el-tag v-if="dish.isRecommend === 1" type="danger" class="recommend-tag">推荐</el-tag>
                </div>
                <div class="dish-info">
                  <h3>{{ dish.name }}</h3>
                  <p class="dish-desc">{{ dish.description }}</p>
                  <div class="dish-footer">
                    <span class="dish-price">¥{{ dish.price }}</span>
                    <el-button
                      type="primary"
                      size="small"
                      :disabled="dish.stock === 0 || dish.isAvailable === 0"
                      @click.stop="addToCart(dish)"
                    >
                      加入购物车
                    </el-button>
                  </div>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
          <!-- 套餐标签页 -->
          <el-tab-pane label="套餐" name="combo">
            <div class="dish-grid">
              <el-card
                v-for="combo in combos"
                :key="combo.id"
                class="dish-card"
                :class="{ 'out-of-stock': combo.isAvailable === 0 }"
                @click="handleComboClick(combo)"
              >
                <div class="dish-image">
                  <el-image
                    :src="combo.image || '/placeholder.png'"
                    fit="cover"
                    style="width: 100%; height: 150px"
                  >
                    <template #error>
                      <div class="image-slot">暂无图片</div>
                    </template>
                  </el-image>
                </div>
                <div class="dish-info">
                  <h3>{{ combo.name }}</h3>
                  <p class="dish-desc">{{ combo.description }}</p>
                  <div class="dish-footer">
                    <span class="dish-price">¥{{ combo.price }}</span>
                    <el-button
                      type="primary"
                      size="small"
                      :disabled="combo.isAvailable === 0"
                      @click.stop="addToCart(combo, true)"
                    >
                      加入购物车
                    </el-button>
                  </div>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-main>
      <el-main v-else>
        <div class="loading-container">
          <el-skeleton :rows="6" animated />
        </div>
      </el-main>
    </el-container>
    
    <!-- 购物车抽屉 -->
    <el-drawer
      v-model="showCart"
      title="购物车"
      size="400px"
      :before-close="handleCartClose"
    >
      <div class="cart-content">
        <div v-if="cartStore.items.length === 0" class="empty-cart">
          <el-empty description="购物车是空的" />
        </div>
        <div v-else>
          <div class="cart-items">
            <div
              v-for="item in cartStore.items"
              :key="item.dish?.id || item.combo?.id"
              class="cart-item"
            >
              <div class="item-info">
                <h4>{{ item.dish?.name || item.combo?.name }}</h4>
                <p>¥{{ (item.dish?.price || item.combo?.price) }} × {{ item.quantity }}</p>
              </div>
              <div class="item-actions">
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="item.dish?.stock || 99"
                  size="small"
                  @change="cartStore.updateQuantity(
                    item.dish?.id || item.combo?.id, 
                    item.quantity, 
                    !!item.combo
                  )"
                />
                <el-button
                  type="danger"
                  size="small"
                  :icon="Delete"
                  @click="cartStore.removeItem(
                    item.dish?.id || item.combo?.id, 
                    !!item.combo
                  )"
                />
              </div>
            </div>
          </div>
          <div class="cart-footer">
            <div class="cart-total">
              <span>总计：</span>
              <span class="total-price">¥{{ cartStore.getTotalPrice().toFixed(2) }}</span>
            </div>
            <el-button
              type="primary"
              size="large"
              style="width: 100%"
              @click="handleSubmitOrder"
            >
              提交订单
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>
    
    <!-- 提交订单对话框 -->
    <el-dialog
      v-model="showOrderDialog"
      title="确认订单"
      width="500px"
    >
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="桌号">
          <el-input v-model="tableNumber" disabled />
        </el-form-item>
        <el-form-item label="会员手机号（可选）">
          <el-input
            v-model="orderForm.memberPhone"
            placeholder="输入会员手机号享受折扣"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="orderForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
        <el-form-item label="订单明细">
          <div class="order-items">
            <div
              v-for="item in cartStore.items"
              :key="item.dish?.id || item.combo?.id"
              class="order-item"
            >
              <span>{{ item.dish?.name || item.combo?.name }} × {{ item.quantity }}</span>
              <span>¥{{ ((item.dish?.price || item.combo?.price) * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="订单总额">
          <span class="order-total">¥{{ cartStore.getTotalPrice().toFixed(2) }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showOrderDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder">
          确认提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, ShoppingCart } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { getCategoriesApi } from '@/api/category'
import { getDishesApi, getDishesByCategoryApi } from '@/api/dish'
import { getCombosApi } from '@/api/combo'
import { createOrderApi } from '@/api/order'
import { validateTableNumberApi } from '@/api/table'
import type { Category, DishVO, Combo, OrderVO } from '@/types/api'
import { connectOrderStatusWebSocket, disconnectWebSocket } from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const tableNumber = ref(route.params.tableNumber as string)
const tableValidated = ref(false) // 添加验证状态

// 验证桌号是否存在
const validateTable = async () => {
  try {
    const res = await validateTableNumberApi(tableNumber.value)
    if (!res.data) { // 桌号不存在
      ElMessage.error('桌号不存在，请检查桌号或联系服务员')
      router.push('/customer') // 返回顾客首页
      return false
    }
    return true
  } catch (error) {
    ElMessage.error('验证桌号时发生错误')
    router.push('/customer') // 返回顾客首页
    return false
  }
}

const categories = ref<Category[]>([])
const dishes = ref<DishVO[]>([])
const allDishes = ref<DishVO[]>([])
const combos = ref<Combo[]>([])
const activeCategory = ref('all') // 默认显示所有菜品
const showCart = ref(false)
const showOrderDialog = ref(false)
const submitting = ref(false)

const orderForm = ref({
  memberPhone: '',
  remark: ''
})

onMounted(async () => {
  // 验证桌号是否存在
  const isValid = await validateTable()
  if (isValid) {
    cartStore.tableNumber = tableNumber.value
    tableValidated.value = true // 设置验证状态为true
    await loadCategories()
    await loadCombos() // 加载套餐
    await loadAllDishes() // 加载所有菜品
  } else {
    tableValidated.value = false // 设置验证状态为false
  }
})

const loadCategories = async () => {
  try {
    const res = await getCategoriesApi()
    categories.value = res.data
    if (categories.value.length > 0 && activeCategory.value === categories.value[0].id.toString()) {
      // 只有当当前激活的分类是第一个分类时才加载
      await loadDishes(Number(activeCategory.value))
    }
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

const loadCombos = async () => {
  try {
    const res = await getCombosApi()
    combos.value = res.data
  } catch (error) {
    ElMessage.error('加载套餐失败')
  }
}

const handleCategoryChange = async (categoryId: string) => {
  if (categoryId === 'all') {
    await loadAllDishes()
  } else if (categoryId !== 'combo') {
    await loadDishes(Number(categoryId))
  }
}

const loadAllDishes = async () => {
  try {
    const res = await getDishesApi()
    allDishes.value = res.data
  } catch (error) {
    ElMessage.error('加载所有菜品失败')
  }
}

const loadDishes = async (categoryId: number) => {
  try {
    const res = await getDishesByCategoryApi(categoryId)
    dishes.value = res.data
  } catch (error) {
    ElMessage.error('加载分类菜品失败')
  }
}

const handleDishClick = (dish: DishVO) => {
  if (dish.stock > 0 && dish.isAvailable === 1) {
    addToCart(dish)
  }
}

const handleComboClick = (combo: Combo) => {
  if (combo.isAvailable === 1) {
    addToCart(combo, true)
  }
}

const addToCart = (item: DishVO | Combo, isCombo: boolean = false) => {
  if (isCombo) {
    if ((item as Combo).isAvailable === 0) {
      ElMessage.warning('该套餐暂时不可用')
      return
    }
    cartStore.addItem(item, 1, true)
    ElMessage.success('已加入购物车')
  } else {
    if ((item as DishVO).stock === 0 || (item as DishVO).isAvailable === 0) {
      ElMessage.warning('该菜品暂时不可用')
      return
    }
    cartStore.addItem(item, 1, false)
    ElMessage.success('已加入购物车')
  }
}

const handleCartClose = () => {
  showCart.value = false
}

const handleSubmitOrder = () => {
  if (cartStore.items.length === 0) {
    ElMessage.warning('购物车是空的')
    return
  }
  showOrderDialog.value = true
}

// 当前订单ID
const currentOrderId = ref<number | null>(null)
    
const submitOrder = async () => {
  submitting.value = true
  try {
    const res = await createOrderApi({
      tableNumber: tableNumber.value,
      memberPhone: orderForm.value.memberPhone || undefined,
      remark: orderForm.value.remark || undefined,
      items: cartStore.toOrderItems()
    })

    const order: OrderVO = res.data
        
    // 保存当前订单ID
    currentOrderId.value = order.id
        
    ElMessage.success(`订单提交成功！订单号：${order.orderNumber}`)
    cartStore.clearCart()
    showOrderDialog.value = false
    showCart.value = false

    // 订阅该订单的状态变更
    connectOrderStatusWebSocket(order.id, (updated: OrderVO) => {
      if (updated.status === 'PENDING') {
        ElMessage.info('订单已接收，正在处理中')
      } else if (updated.status === 'CONFIRMED') {
        ElMessage.info('订单已确认，开始备餐')
      } else if (updated.status === 'PREPARING') {
        ElMessage.info('正在为您准备菜品')
      } else if (updated.status === 'READY') {
        ElMessageBox.alert(
          `您的订单【${updated.orderNumber}】已完成，请前往前台结账或等待服务员上菜。`,
          '订单完成',
          {
            confirmButtonText: '我知道了',
            type: 'success'
          }
        )
        // 清除当前订单ID
        currentOrderId.value = null
      }
    })
  } catch (error: any) {
    ElMessage.error(error.message || '提交订单失败')
  } finally {
    submitting.value = false
  }
}

onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
.table-order {
  min-height: 100vh;
  background: #f5f7fa;
}

.order-header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-left h2 {
  margin: 0;
  color: #333;
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.dish-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.dish-card:hover {
  transform: translateY(-5px);
}

.dish-card.out-of-stock {
  opacity: 0.6;
}

.dish-image {
  position: relative;
}

.recommend-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 150px;
  background: #f5f7fa;
  color: #909399;
}

.dish-info {
  padding: 10px;
}

.dish-info h3 {
  margin: 0 0 5px;
  font-size: 16px;
  color: #333;
}

.dish-desc {
  margin: 5px 0;
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.dish-price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.cart-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.empty-cart {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-items {
  flex: 1;
  overflow-y: auto;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.item-info h4 {
  margin: 0 0 5px;
  font-size: 14px;
}

.item-info p {
  margin: 0;
  color: #666;
  font-size: 12px;
}

.item-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.cart-footer {
  padding: 20px;
  border-top: 1px solid #eee;
  background: #fff;
}

.cart-total {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 18px;
}

.total-price {
  color: #f56c6c;
  font-weight: bold;
}

.order-items {
  max-height: 200px;
  overflow-y: auto;
}

.order-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.order-total {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.order-status {
  margin-bottom: 20px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}
</style>