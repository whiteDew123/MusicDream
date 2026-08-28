const { recommendSongsApi, rankSongsApi, recommendArtistsApi } = require('../../api/music')
const { publicSongListApi } = require('../../api/songList')
const { getUserInfo } = require('../../utils/auth')
const { formatPlays } = require('../../utils/format')
const { getPlayer } = require('../../utils/player')
const { decorateSongs, decorateLists, decorateArtists } = require('../../utils/url')

Page({
  data: {
    loading: true,
    recommendSongs: [],
    rankSongs: [],
    songLists: [],
    recommendArtists: []
  },

  onShow() {
    this.loadData()
  },

  loadData() {
    this.setData({ loading: true })
    const userId = (getUserInfo() || {}).userId
    Promise.all([
      recommendSongsApi({ userId, limit: 6 }),
      rankSongsApi(5),
      publicSongListApi(),
      recommendArtistsApi(6)
    ])
      .then(([a, b, c, d]) => {
        this.setData({
          recommendSongs: decorateSongs(a.data || []).map((it) => ({
            ...it,
            playsText: formatPlays(it.listenNumb)
          })),
          rankSongs: decorateSongs(b.data || []).map((it) => ({
            ...it,
            playsText: formatPlays(it.listenNumb)
          })),
          songLists: decorateLists(c.data || []),
          recommendArtists: decorateArtists(d.data || [])
        })
      })
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  },

  onPlayRecommend() {
    if (this.data.recommendSongs.length) {
      getPlayer().setPlaylist(this.data.recommendSongs, 0)
      wx.showToast({ title: '开始播放推荐歌曲', icon: 'none' })
    } else {
      wx.showToast({ title: '暂无推荐歌曲', icon: 'none' })
    }
  },

  onSongTap(e) {
    const idx = e.currentTarget.dataset.idx
    const song = this.data.recommendSongs[idx]
    if (song) getPlayer().playSong(song)
  },

  onRankTap(e) {
    const idx = e.currentTarget.dataset.idx
    const song = this.data.rankSongs[idx]
    if (song) getPlayer().playSong(song)
  },

  onArtistTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/singer-detail/singer-detail?id=' + id })
  },

  onPlaylistTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/songlist-detail/songlist-detail?id=' + id })
  },

  goRank() {
    wx.navigateTo({ url: '/pages/rank/rank' })
  },
  goSongList() {
    wx.switchTab({ url: '/pages/songlist/songlist' })
  },
  goSinger() {
    wx.navigateTo({ url: '/pages/singer/singer' })
  },

  onShareAppMessage() {
    return { title: 'MusicDreamer · 让音乐流动', path: '/pages/discovery/discovery' }
  }
})
