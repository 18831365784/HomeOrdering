<template>
  <image
    :src="displaySrc"
    :mode="mode"
    :class="imgClass"
  />
</template>

<script>
export default {
  name: 'SafeImage',
  props: {
    src: { type: String, default: '' },
    mode: { type: String, default: 'aspectFill' },
    imgClass: { type: String, default: '' }
  },
  data() {
    return {
      displaySrc: ''
    }
  },
  watch: {
    src: {
      immediate: true,
      handler(newVal) {
        this.loadImage(newVal)
      }
    }
  },
  methods: {
    loadImage(url) {
      if (!url) {
        this.displaySrc = ''
        return
      }
      // 仅对 ngrok-free 域名使用带 header 的下载再显示
      if (url.includes('ngrok-free.dev')) {
        uni.downloadFile({
          url,
          header: { 'ngrok-skip-browser-warning': 'true' },
          success: (res) => {
            if (res.statusCode === 200 && res.tempFilePath) {
              this.displaySrc = res.tempFilePath
            } else {
              this.displaySrc = url
            }
          },
          fail: () => {
            this.displaySrc = url
          }
        })
      } else {
        this.displaySrc = url
      }
    }
  }
}
</script>

<style scoped>
</style>


