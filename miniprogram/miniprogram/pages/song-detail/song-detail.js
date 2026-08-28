const { songDetailApi } = require('../../api/music')
const {
  getMusicStatsApi,
  toggleLikeApi,
  commentListApi,
  createCommentApi
} = require('../../api/interaction')
const { toggleCollect, addToSongList } = require('../../utils/songActions')
const { isLogin } = require('../../utils/auth')
const { resolveUrl } = require('../../utils/url')

Page({
  data: {
    loading: true,
    song: null,
    liked: false,
    likesCount: 0,
    collected: false,
    comments: [],
    commentTotal: 0,
    commentText: '',
    submitting: false
  },

  onLoad(options) {
    this.musicId = Number(options.musicId)
    this.load()
  },

  load() {
    this.setData({ loading: true })
    Promise.all([songDetailApi(this.musicId), getMusicStatsApi(this.musicId)])
      .then(([d, stats]) => {
        const st = stats.data || {}
        const song = d.data || null
        this.setData({
          song: song ? { ...song, imageUrl: resolveUrl(song.imageUrl) } : null,
          liked: !!st.liked,
          likesCount: st.likesCount || 0
        })
        wx.setNavigationBarTitle({ title: (d.data || {}).musicName || '歌曲详情' })
      })
      .catch(() => {})
      .then(() => this.loadComments())
      .finally(() => this.setData({ loading: false }))
  },

  loadComments() {
    commentListApi(this.musicId, 1, 50)
      .then((r) => {
        const page = r.data || {}
        this.setData({ comments: page.records || [], commentTotal: page.total || 0 })
      })
      .catch(() => {})
  },

  onLike() {
    if (!this.data.song) return
    toggleLikeApi(this.musicId)
      .then((r) => {
        const d = r.data || {}
        this.setData({ liked: !!d.liked, likesCount: d.likesCount })
      })
      .catch(() => {})
  },

  onCollect() {
    if (!this.data.song) return
    toggleCollect(this.data.song).then((liked) => {
      if (typeof liked === 'boolean') this.setData({ collected: liked })
    })
  },

  onAddToSongList() {
    if (!this.data.song) return
    addToSongList(this.data.song)
  },

  onCommentInput(e) {
    this.setData({ commentText: e.detail.value })
  },

  onSubmitComment() {
    const content = this.data.commentText.trim()
    if (!content) {
      wx.showToast({ title: '请输入评论', icon: 'none' })
      return
    }
    if (!isLogin()) {
      wx.navigateTo({ url: '/pages/login/login?redirect=1' })
      return
    }
    this.setData({ submitting: true })
    createCommentApi(this.musicId, { content })
      .then(() => {
        this.setData({ commentText: '' })
        wx.showToast({ title: '评论成功', icon: 'none' })
        this.loadComments()
      })
      .catch(() => {})
      .finally(() => this.setData({ submitting: false }))
  },

  onShareAppMessage() {
    const s = this.data.song
    return {
      title: s ? s.musicName + ' - ' + s.singerName : 'MusicDreamer',
      path: '/pages/song-detail/song-detail?musicId=' + this.musicId
    }
  }
})
