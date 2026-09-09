// Preload 脚本 - 安全桥接 Electron API
// 只暴露受控方法给渲染进程（playerFront Vue3）

const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('electron', {
  isElectron: true,

  // 悬浮窗管理
  openLyricsWindow: () => ipcRenderer.send('open-lyrics-window'),
  closeLyricsWindow: () => ipcRenderer.send('close-lyrics-window'),

  // 主窗口 → 悬浮窗：推送数据
  sendSongChange:  (data) => ipcRenderer.send('song-change', data),
  sendLyricsUpdate:(data) => ipcRenderer.send('lyrics-update', data),
  sendPlayState:   (data) => ipcRenderer.send('play-state-change', data),
  sendProgress:    (data) => ipcRenderer.send('progress-update', data),
  sendCover:       (data) => ipcRenderer.send('cover-update', data),

  // 悬浮窗 → 主窗口：播放控制
  onRemotePlayPause: (cb) => ipcRenderer.on('remote-play-pause', cb),
  onRemotePrev:      (cb) => ipcRenderer.on('remote-prev', cb),
  onRemoteNext:      (cb) => ipcRenderer.on('remote-next', cb),

  // 主进程 → 主窗口：悬浮窗已就绪（通知 Vue 重推状态）
  onLyricsWindowReady: (cb) => ipcRenderer.on('lyrics-window-ready', cb)
})
