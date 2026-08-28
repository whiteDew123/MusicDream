/**
 * 歌曲行内操作助手（收藏/下一首/详情 + 分享标记）
 * - 收藏/取消收藏走 like/music（区别于点赞 like/song）
 * - 需登录校验，未登录跳登录页
 */
const { isLogin } = require('./auth')
const { likedMusicApi, addLikedMusicApi, removeLikedMusicApi } = require('../api/like')
const { shareSongApi } = require('../api/interaction')
const { getPlayer } = require('./player')
const { myCreatedSongListApi, addMusicToSongListApi } = require('../api/songList')

function requireLogin() {
  if (isLogin()) return true
  wx.navigateTo({ url: '/pages/login/login?redirect=1' })
  return false
}

// 切换收藏（歌曲）: 返回最终是否收藏
async function toggleCollect(song) {
  if (!requireLogin()) return null
  wx.showLoading({ title: '处理中' })
  try {
    const res = await likedMusicApi()
    const list = res.data || []
    const exists = list.some((s) => s.musicId === song.musicId)
    if (exists) {
      await removeLikedMusicApi(song.musicId)
    } else {
      await addLikedMusicApi(song.musicId)
    }
    wx.hideLoading()
    wx.showToast({ title: exists ? '已取消收藏' : '已收藏', icon: 'none' })
    return !exists
  } catch (e) {
    wx.hideLoading()
    return null
  }
}

// 下一首播放
function playNext(song) {
  const player = getPlayer()
  player.addToPlaylist(song)
  player.playNext()
  wx.showToast({ title: '已加入下一首', icon: 'none' })
}

// 记录分享行为
async function recordShare(musicId) {
  try {
    await shareSongApi(musicId)
  } catch (e) {
    // 静默
  }
}

// 添加到我的歌单
async function addToSongList(song) {
  if (!requireLogin()) return
  try {
    const r = await myCreatedSongListApi()
    const list = r.data || []
    if (!list.length) {
      wx.showToast({ title: '还没有歌单，请先创建', icon: 'none' })
      return
    }
    wx.showActionSheet({
      itemList: list.map((x) => x.name),
      success: async (res) => {
        const target = list[res.tapIndex]
        if (!target) return
        await addMusicToSongListApi({ listId: target.id, musicId: song.musicId })
        wx.showToast({ title: '已加入歌单', icon: 'none' })
      }
    })
  } catch (e) {
    // 静默
  }
}

// 弹出行内操作面板
function showSongActions(song) {
  if (!song) return
  wx.showActionSheet({
    itemList: ['下一首播放', '收藏/取消收藏', '歌曲详情'],
    success: (res) => {
      if (res.tapIndex === 0) {
        playNext(song)
      } else if (res.tapIndex === 1) {
        toggleCollect(song).then((collected) => {
          if (typeof collected === 'boolean' && collected) {
            wx.showToast({ title: '已收藏', icon: 'none' })
          }
        })
      } else if (res.tapIndex === 2) {
        wx.navigateTo({ url: '/pages/song-detail/song-detail?musicId=' + song.musicId })
      }
    }
  })
}

module.exports = {
  toggleCollect,
  playNext,
  recordShare,
  showSongActions,
  addToSongList,
  requireLogin
}
