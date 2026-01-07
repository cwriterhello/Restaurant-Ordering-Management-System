import SockJS from 'sockjs-client'
import Stomp, { Client, Message } from 'stompjs'

let stompClient: Client | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null

type SubscribeFn = (client: Client) => void
const subscribers: SubscribeFn[] = []

// 兼容旧的厨房端调用：只关心后厨订单
export const connectWebSocket = (onKitchenOrder: (order: any) => void) => {
  addSubscriber((client) => {
    client.subscribe('/topic/kitchen/orders', (message: Message) => {
      const order = JSON.parse(message.body)
      onKitchenOrder(order)
    })
  })
  ensureConnected()
}

// 管理端订阅库存预警
export const connectStockAlertWebSocket = (onAlert: (message: string) => void) => {
  addSubscriber((client) => {
    client.subscribe('/topic/admin/stock-alerts', (message: Message) => {
      const text = message.body
      onAlert(text)
    })
  })
  ensureConnected()
}

// 顾客端订阅指定订单的状态
export const connectOrderStatusWebSocket = (orderId: number, onOrder: (order: any) => void) => {
  addSubscriber((client) => {
    client.subscribe(`/topic/order/status/${orderId}`, (message: Message) => {
      const order = JSON.parse(message.body)
      onOrder(order)
    })
  })
  ensureConnected()
}

const addSubscriber = (fn: SubscribeFn) => {
  subscribers.push(fn)
  if (stompClient && stompClient.connected) {
    fn(stompClient)
  }
}

const ensureConnected = () => {
  if (stompClient && stompClient.connected) return

  const socket = new SockJS('/ws')
  stompClient = Stomp.over(socket)

  stompClient.connect(
    {},
    () => {
      console.log('WebSocket连接成功')
      subscribers.forEach((fn) => fn(stompClient!))
    },
    (error: any) => {
      console.error('WebSocket连接失败', error)
      if (reconnectTimer) {
        clearTimeout(reconnectTimer)
      }
      reconnectTimer = setTimeout(() => {
        ensureConnected()
      }, 5000)
    }
  )
}

export const disconnectWebSocket = () => {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (stompClient) {
    stompClient.disconnect(() => {
      console.log('WebSocket已断开')
    })
    stompClient = null
  }
}


