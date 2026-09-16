<template>
  <image
    :src="displaySrc"
    :mode="mode"
    :class="imgClass"
  />
</template>

<script>
import { getApiBaseUrl } from '@/utils/api.js'

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
      const base = getApiBaseUrl() || ''
      const needNgrokHeader = base.includes('ngrok') || url.includes('ngrok')
      if (needNgrokHeader) {
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
