// Electron 主进程 - MusicDreamer 桌面版 v4.0
// 两个窗口 + 双向 IPC + 位置/样式持久化 + 穿透/置顶 + 迷你/歌词双模式

const { app, BrowserWindow, ipcMain, screen } = require('electron')
const path = require('path')
const fs = require('fs')

const isDev = process.env.NODE_ENV !== 'production'
const DEV_URL = process.env.VITE_DEV_SERVER_URL || 'http://localhost:5174'
const PROD_URL = path.join(__dirname, '..', 'playerFront', 'dist', 'index.html')

let mainWindow = null
let lyricsWindow = null

const POS_FILE = path.join(app.getPath('userData'), 'lyrics-pos.json')
const STYLE_FILE = path.join(app.getPath('userData'), 'lyrics-style.json')

function loadSavedBounds() {
  try { if (fs.existsSync(POS_FILE)) return JSON.parse(fs.readFileSync(POS_FILE, 'utf8')) }
  catch (e) { console.warn('读取位置失败:', e.message) }
  return null
}
function saveBounds() {
  if (!lyricsWindow || lyricsWindow.isDestroyed()) return
  try { fs.writeFileSync(POS_FILE, JSON.stringify(lyricsWindow.getBounds())) }
  catch (e) { console.warn('保存位置失败:', e.message) }
}

// ===== 主窗口 =====
function createMainWindow() {
  mainWindow = new BrowserWindow({
    width: 1280, height: 720,
    minWidth: 1024, minHeight: 640,
    title: 'MusicDreamer',
    backgroundColor: '#f6f9fc',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  })
  if (isDev) { mainWindow.loadURL(DEV_URL); mainWindow.webContents.openDevTools({ mode: 'detach' }) }
  else { mainWindow.loadFile(PROD_URL) }
  mainWindow.on('closed', () => {
    mainWindow = null
    if (lyricsWindow && !lyricsWindow.isDestroyed()) lyricsWindow.close()
  })
}

// ===== 悬浮歌词窗口 =====
// 迷你模式: ~56px 高   歌词模式: ~180px 高
function createLyricsWindow() {
  if (lyricsWindow && !lyricsWindow.isDestroyed()) {
    lyricsWindow.show()
    lyricsWindow.setAlwaysOnTop(true, 'screen-saver')
    return
  }
  const saved = loadSavedBounds()
  const workArea = screen.getPrimaryDisplay().workArea

  lyricsWindow = new BrowserWindow({
    width: saved?.width || 480,
    height: saved?.height || 60,
    minWidth: 360, minHeight: 56,
    maxWidth: 1200, maxHeight: 220,
    x: saved?.x ?? (workArea.x + workArea.width / 2 - 240),
    y: saved?.y ?? (workArea.y + workArea.height - 160),
    frame: false,
    transparent: true,
    alwaysOnTop: true,
    skipTaskbar: true,
    resizable: true,
    minimizable: false, maximizable: false,
    hasShadow: false,
    roundedCorners: true,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false,
      webSecurity: false,
      allowRunningInsecureContent: true
    }
  })
  lyricsWindow.setAlwaysOnTop(true, 'screen-saver')
  lyricsWindow.loadFile(path.join(__dirname, 'lyrics-window', 'index.html'))
  lyricsWindow.on('moved', saveBounds)
  lyricsWindow.on('resized', saveBounds)
  lyricsWindow.on('close', () => { saveBounds(); lyricsWindow = null })
}

// ===== IPC：主窗口 → 悬浮窗 =====
function broadcast(...args) {
  if (lyricsWindow && !lyricsWindow.isDestroyed()) {
    lyricsWindow.webContents.send(...args)
  }
}

ipcMain.on('open-lyrics-window', createLyricsWindow)
ipcMain.on('close-lyrics-window', () => { if (lyricsWindow && !lyricsWindow.isDestroyed()) lyricsWindow.hide() })
ipcMain.on('close-lyrics', () => { if (lyricsWindow && !lyricsWindow.isDestroyed()) lyricsWindow.hide() })

ipcMain.on('song-change',       (_e, data) => broadcast('song-change', data))
ipcMain.on('lyrics-update',     (_e, data) => broadcast('lyrics', data))
ipcMain.on('play-state-change', (_e, data) => broadcast('play-state', data))
ipcMain.on('progress-update',   (_e, data) => broadcast('progress', data))
ipcMain.on('cover-update',      (_e, data) => broadcast('cover', data))

// ===== IPC：悬浮窗 → 主窗口 =====
ipcMain.on('remote-play-pause', () => {
  if (mainWindow && !mainWindow.isDestroyed()) mainWindow.webContents.send('remote-play-pause')
})
ipcMain.on('remote-prev', () => {
  if (mainWindow && !mainWindow.isDestroyed()) mainWindow.webContents.send('remote-prev')
})
ipcMain.on('remote-next', () => {
  if (mainWindow && !mainWindow.isDestroyed()) mainWindow.webContents.send('remote-next')
})
ipcMain.on('lyrics-window-ready', () => {
  if (mainWindow && !mainWindow.isDestroyed()) mainWindow.webContents.send('lyrics-window-ready')
})

// ===== 悬浮窗自身控制 =====
ipcMain.on('toggle-passthrough', (_e, on) => {
  if (lyricsWindow && !lyricsWindow.isDestroyed()) {
    lyricsWindow.setIgnoreMouseEvents(!!on, { forward: true })
  }
})
ipcMain.on('toggle-ontop', (_e, on) => {
  if (lyricsWindow && !lyricsWindow.isDestroyed()) {
    lyricsWindow.setAlwaysOnTop(!!on, 'screen-saver')
  }
})
ipcMain.on('show-main-window', () => {
  if (mainWindow && !mainWindow.isDestroyed()) {
    if (mainWindow.isMinimized()) mainWindow.restore()
    mainWindow.show()
    mainWindow.focus()
  }
})

// ===== 渲染层请求调整窗口尺寸（模式切换时自动 resize）=====
ipcMain.on('set-lyrics-bounds', (_e, { width, height }) => {
  if (!lyricsWindow || lyricsWindow.isDestroyed()) return
  const [cx, cy] = [
    lyricsWindow.getBounds().x + lyricsWindow.getBounds().width / 2,
    lyricsWindow.getBounds().y + lyricsWindow.getBounds().height / 2
  ]
  lyricsWindow.setBounds({
    x: Math.round(cx - width / 2),
    y: Math.round(cy - height / 2),
    width, height
  })
})

// ===== 样式持久化 =====
ipcMain.handle('get-lyrics-style', () => {
  try { if (fs.existsSync(STYLE_FILE)) return JSON.parse(fs.readFileSync(STYLE_FILE, 'utf8')) }
  catch (e) { return null }
  return null
})
ipcMain.on('save-lyrics-style', (_e, style) => {
  try { fs.writeFileSync(STYLE_FILE, JSON.stringify(style, null, 2)) }
  catch (e) { console.warn('保存样式失败:', e.message) }
})

// ===== 生命周期 =====
app.whenReady().then(() => {
  app.commandLine.appendSwitch('high-dpi-support', '1')
  createMainWindow()
  app.on('activate', () => { if (BrowserWindow.getAllWindows().length === 0) createMainWindow() })
})
app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit() })
