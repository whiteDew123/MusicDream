const { getPlayer } = require('../../utils/player')
const { formatDuration } = require('../../utils/format')
const { resolveUrl } = require('../../utils/url')

Component({
  options: { styleIsolation: 'apply-shared' },
  data: {
    song: null,
    cover: '',
    playing: false,
    durationText: '',
    progress: 0
  },
  lifetimes: {
    attached() {
      this._handler = (snap) => this.onChange(snap)
      getPlayer().on(this._handler)
      this.onChange(getPlayer().getSnapshot())
    },
    detached() {
      if (this._handler) getPlayer().off(this._handler)
    }
  },
  methods: {
    onChange(snap) {
      const progress = snap.duration ? Math.min(100, (snap.currentTime / snap.duration) * 100) : 0
      this.setData({
        song: snap.song,
        cover: snap.song ? resolveUrl(snap.song.imageUrl) : '',
        playing: snap.playing,
        progress,
        durationText: formatDuration(snap.song ? snap.song.timelength : snap.duration)
      })
    },
    onTogglePlay() {
      getPlayer().togglePlay()
    },
    onPrev() {
      getPlayer().playPrev()
    },
    onNext() {
      getPlayer().playNext()
    },
    onOpen() {
      if (this.data.song) wx.navigateTo({ url: '/pages/player/player' })
    }
  }
})
