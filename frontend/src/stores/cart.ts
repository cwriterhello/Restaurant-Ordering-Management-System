import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DishVO, OrderItemDTO, Combo } from '@/types/api'

interface CartItem {
  dish?: DishVO
  combo?: Combo
  quantity: number
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])
  const tableNumber = ref<string>('')

  const addItem = (item: DishVO | Combo, quantity: number = 1, isCombo: boolean = false) => {
    const existingItem = items.value.find(cartItem => 
      isCombo ? 
        (cartItem.combo && cartItem.combo.id === (item as Combo).id) : 
        (cartItem.dish && cartItem.dish.id === (item as DishVO).id)
    )
    
    if (existingItem) {
      existingItem.quantity += quantity
    } else {
      items.value.push({
        dish: isCombo ? undefined : (item as DishVO),
        combo: isCombo ? (item as Combo) : undefined,
        quantity
      })
    }
  }

  const removeItem = (itemId: number, isCombo: boolean = false) => {
    const index = items.value.findIndex(cartItem => 
      isCombo ? 
        (cartItem.combo && cartItem.combo.id === itemId) : 
        (cartItem.dish && cartItem.dish.id === itemId)
    )
    if (index > -1) {
      items.value.splice(index, 1)
    }
  }

  const updateQuantity = (itemId: number, quantity: number, isCombo: boolean = false) => {
    const item = items.value.find(cartItem => 
      isCombo ? 
        (cartItem.combo && cartItem.combo.id === itemId) : 
        (cartItem.dish && cartItem.dish.id === itemId)
    )
    if (item) {
      if (quantity <= 0) {
        removeItem(itemId, isCombo)
      } else {
        item.quantity = quantity
      }
    }
  }

  const clearCart = () => {
    items.value = []
    tableNumber.value = ''
  }

  const getTotalPrice = () => {
    return items.value.reduce((total, item) => {
      if (item.dish) {
        return total + item.dish.price * item.quantity
      } else if (item.combo) {
        return total + item.combo.price * item.quantity
      }
      return total
    }, 0)
  }

  const getTotalCount = () => {
    return items.value.reduce((total, item) => total + item.quantity, 0)
  }

  const toOrderItems = (): OrderItemDTO[] => {
    return items.value.map(item => {
      if (item.dish) {
        return {
          itemType: 'DISH',
          itemId: item.dish.id,
          quantity: item.quantity
        }
      } else if (item.combo) {
        return {
          itemType: 'COMBO',
          itemId: item.combo.id,
          quantity: item.quantity
        }
      }
      return {
        itemType: 'DISH',
        itemId: 0,
        quantity: 0
      }
    })
  }

  return {
    items,
    tableNumber,
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    getTotalPrice,
    getTotalCount,
    toOrderItems
  }
})