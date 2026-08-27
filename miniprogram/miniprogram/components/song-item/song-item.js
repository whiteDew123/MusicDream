const { formatPlays, formatDuration } = require('../../utils/format')
const { showSongActions } = require('../../utils/songActions')
const { getPlayer } = require('../../utils/player')
const { resolveUrl } = require('../../utils/url')

Component({
  options: { styleIsolation: 'apply-shared' },
  properties: {
    song: { type: Object, value: null },
    index: { type: Number, value: 0 },
    showRank: { type: Boolean, value: false },
    showPlays: { type: Boolean, value: true },
    active: { type: Boolean, value: false }
  },
  data: {
    playsText: '',
    durationText: '',
    cover: ''
  },
  observers: {
    song: function (song) {
      if (!song) return
      this.setData({
        playsText: formatPlays(song.listenNumb),
        durationText: formatDuration(song.timelength),
        cover: resolveUrl(song.imageUrl)
      })
    }
  },
  methods: {
    onTap() {
      if (!this.data.song) return
      getPlayer().playSong(this.data.song)
      this.triggerEvent('play', { song: this.data.song })
    },
    onMore() {
      if (this.data.song) showSongActions(this.data.song)
    }
  }
})
