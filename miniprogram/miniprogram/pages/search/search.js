const { searchSongsApi } = require('../../api/music')

const HISTORY_KEY = 'MD_SEARCH_HISTORY'

Page({
  data: {
    keyword: '',
    history: [],
    results: [],
    searched: false,
    loading: false
  },

  onLoad() {
    this.setData({ history: wx.getStorageSync(HISTORY_KEY) || [] })
  },

  onInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    const kw = this.data.keyword.trim()
    if (!kw) {
      wx.showToast({ title: '请输入关键词', icon: 'none' })
      return
    }
    this.setData({ loading: true, searched: true })
    searchSongsApi({ keyword: kw, page: 1, size: 20 })
      .then((r) => {
        this.setData({ results: r.data || [] })
        this.saveHistory(kw)
      })
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  },

  saveHistory(kw) {
    let h = this.data.history.filter((x) => x !== kw)
    h.unshift(kw)
    h = h.slice(0, 10)
    this.setData({ history: h })
    wx.setStorageSync(HISTORY_KEY, h)
  },

  onHistory(e) {
    const kw = e.currentTarget.dataset.kw
    this.setData({ keyword: kw })
    this.onSearch()
  },

  clearHistory() {
    this.setData({ history: [] })
    wx.removeStorageSync(HISTORY_KEY)
  },

  onClearInput() {
    this.setData({ keyword: '', results: [], searched: false })
  },

  onShareAppMessage() {
    return { title: 'MusicDreamer · 搜索', path: '/pages/search/search' }
  }
})
