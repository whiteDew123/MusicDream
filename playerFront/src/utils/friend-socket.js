import { Client } from '@stomp/stompjs'
import { getToken } from '@/utils/auth'

/**
 * 好友消息 WebSocket 连接器（基于 @stomp/stompjs）
 * <p>
 * - 连接网关的 /ws-friend（经 Vite 代理到网关，网关再转发到 mod-friend 的 STOMP 端点）
 * - CONNECT 帧携带 token，mod-friend 校验后注入 Principal
 * - 订阅 /user/queue/friend/new 接收新消息推送（点对点，每个用户只收自己的消息）
 * - 发送消息用 publish('/app/friend/message/send', {...})
 * - 自动指数退避重连（1s → 2s → 4s → 最大 30s）
 */
export function createFriendSocket(options) {
  const { onNewMessage, onStatus } = options || {}
  let client = null
  let reconnectAttempts = 0
  let reconnectTimer = null
  let disposed = false
  let connectedAt = 0

  function wsUrl() {
    const proto = location.protocol === 'https:' ? 'wss' : 'ws'
    return `${proto}://${location.host}/ws-friend`
  }

  function connect() {
    if (disposed) return
    client = new Client({
      brokerURL: wsUrl(),
      connectHeaders: {
        token: getToken() || ''
      },
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      reconnectDelay: 0 // 手动指数退避
    })

    client.onConnect = () => {
      connectedAt = Date.now()
      console.log('[FriendWS] 已连接')
      // 订阅自己的新消息队列（STOMP 会自动把 /user/queue 路由到 Principal 对应的用户）
      client.subscribe('/user/queue/friend/new', (m) => {
        try {
          const data = JSON.parse(m.body)
          if (typeof onNewMessage === 'function') onNewMessage(data)
        } catch (e) {
          console.warn('[FriendWS] 解析消息失败:', e)
        }
      })
      if (typeof onStatus === 'function') onStatus(true)
    }

    client.onWebSocketClose = () => {
      console.log('[FriendWS] 连接断开，准备重连...')
      if (typeof onStatus === 'function') onStatus(false)
      scheduleReconnect()
    }

    client.onStompError = (frame) => {
      console.warn('[FriendWS] STOMP 错误:', frame?.headers?.message || frame)
      if (typeof onStatus === 'function') onStatus(false)
      scheduleReconnect()
    }

    client.activate()
  }

  /**
   * 通过 STOMP 发送消息（替代 HTTP POST /friend/message/send）
   * <p>
   * @param {Object} dto  { friendId, content, msgType }
   */
  function sendMessage(dto) {
    if (client && client.connected) {
      client.publish({
        destination: '/app/friend/message/send',
        body: JSON.stringify(dto)
      })
      return true
    }
    return false
  }

  function scheduleReconnect() {
    if (disposed) return
    if (reconnectTimer) return
    if (connectedAt > 0 && Date.now() - connectedAt >= 3000) {
      reconnectAttempts = 0
    }
    const delay = Math.min(1000 * 2 ** reconnectAttempts, 30000)
    reconnectAttempts += 1
    console.log(`[FriendWS] 第 ${reconnectAttempts} 次重连，${(delay / 1000).toFixed(1)}s 后尝试`)
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null
      connect()
    }, delay)
  }

  function disconnect() {
    disposed = true
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (client) {
      try { client.deactivate() } catch (e) {}
      client = null
    }
  }

  connect()

  return {
    sendMessage,
    disconnect,
    get connected() {
      return !!(client && client.connected)
    }
  }
}
