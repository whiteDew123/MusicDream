import { Client } from '@stomp/stompjs'
import { getToken } from '@/utils/auth'

/**
 * 播放室 WebSocket 连接器（基于 @stomp/stompjs）
 * <p>
 * - 连接网关的 /ws（经 Vite 代理到网关，网关再转发到 mod-room 的 STOMP 端点）
 * - CONNECT 帧携带 token，mod-room 校验后注入 Principal
 * - 自动订阅四个广播主题，并处理指数退避重连（1s → 2s → 4s → 8s → 最大 30s）
 * - 每 30s 发送一次心跳，保持在线状态
 */
export function createRoomSocket(options) {
  const { roomId, onState, onMembers, onMessage, onVote, onPlaylist, onRoom, onClosed, onStatus } = options
  let client = null
  let reconnectAttempts = 0
  let heartbeatTimer = null
  let reconnectTimer = null
  let disposed = false
  let connectedAt = 0

  function wsUrl() {
    const proto = location.protocol === 'https:' ? 'wss' : 'ws'
    return `${proto}://${location.host}/ws`
  }

  function connect() {
    if (disposed) return
    client = new Client({
      brokerURL: wsUrl(),
      connectHeaders: {
        token: getToken() || '',
        roomId: String(roomId)
      },
      // 与后端 SimpleBroker 心跳(10s)对齐，空闲时避免被判定失联断开
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      reconnectDelay: 0 // 手动指数退避
    })

    client.onConnect = () => {
      connectedAt = Date.now()
      console.log('[WS] 已连接，订阅房间', roomId)
      client.subscribe(`/topic/room/${roomId}/state`, (m) => safeEmit(onState, m.body))
      client.subscribe(`/topic/room/${roomId}/members`, (m) => safeEmit(onMembers, m.body))
      client.subscribe(`/topic/room/${roomId}/message`, (m) => safeEmit(onMessage, m.body))
      client.subscribe(`/topic/room/${roomId}/skip-vote`, (m) => safeEmit(onVote, m.body))
      client.subscribe(`/topic/room/${roomId}/playlist`, (m) => safeEmit(onPlaylist, m.body))
      client.subscribe(`/topic/room/${roomId}/room`, (m) => safeEmit(onRoom, m.body))
      client.subscribe(`/topic/room/${roomId}/closed`, (m) => safeEmit(onClosed, m.body))
      startHeartbeat()
      if (onStatus) onStatus(true)
      // 连接后立即心跳一次，尽快上线
      publish('/heartbeat', {})
    }

    client.onWebSocketClose = () => {
      console.log('[WS] 连接断开，准备重连...')
      if (onStatus) onStatus(false)
      scheduleReconnect()
    }
    client.onStompError = () => {
      console.log('[WS] STOMP 错误，准备重连...')
      if (onStatus) onStatus(false)
      scheduleReconnect()
    }

    client.activate()
  }

  function publish(path, body) {
    if (client && client.connected) {
      client.publish({
        destination: `/app/room/${roomId}${path}`,
        body: JSON.stringify(body || {})
      })
    }
  }

  function startHeartbeat() {
    stopHeartbeat()
    heartbeatTimer = setInterval(() => publish('/heartbeat', {}), 30000)
  }

  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  function scheduleReconnect() {
    if (disposed) return
    stopHeartbeat()
    if (reconnectTimer) return
    // 上次连接若"刚建就断"（<3s）视为不稳定，退避继续累加，避免 1s 重连风暴；
    // 若曾稳定连接则从 0 开始快速重连
    if (connectedAt <= 0 || Date.now() - connectedAt >= 3000) {
      reconnectAttempts = 0
    }
    const delay = Math.min(1000 * 2 ** reconnectAttempts, 30000)
    reconnectAttempts += 1
    console.log(`[WS] 第 ${reconnectAttempts} 次重连，${(delay / 1000).toFixed(1)}s 后尝试`)
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null
      connect()
    }, delay)
  }

  function disconnect() {
    disposed = true
    stopHeartbeat()
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (client) {
      try {
        client.deactivate()
      } catch (e) {
        // 忽略
      }
      client = null
    }
  }

  function safeEmit(fn, body) {
    if (typeof fn !== 'function') return
    try {
      fn(JSON.parse(body))
    } catch (e) {
      console.warn('解析 WS 消息失败:', e)
    }
  }

  connect()

  return {
    publish,
    disconnect,
    get connected() {
      return !!(client && client.connected)
    }
  }
}