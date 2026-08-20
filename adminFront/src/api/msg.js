import request from '@/api/request'

export function publishMsg(data) {
  return request({
    url: '/msg/publish',
    method: 'post',
    data
  })
}

export function getMyMessages() {
  return request({
    url: '/msg/my',
    method: 'get'
  })
}

export function getUnreadCount() {
  return request({
    url: '/msg/unread-count',
    method: 'get'
  })
}

export function markAsRead(id) {
  return request({
    url: `/msg/read/${id}`,
    method: 'put'
  })
}

export function markAllAsRead() {
  return request({
    url: '/msg/read-all',
    method: 'put'
  })
}