<template>
  <image
    :src="displaySrc"
    :mode="mode"
    :class="imgClass"
  />
</template>

<script>
import { getApiBaseUrl, resolveMediaUrl } from '@/utils/api.js'

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
      const resolved = resolveMediaUrl(url)
      const base = getApiBaseUrl() || ''
      const needNgrokHeader = base.includes('ngrok') || resolved.includes('ngrok')
      if (needNgrokHeader) {
        uni.downloadFile({
          url: resolved,
          header: { 'ngrok-skip-browser-warning': 'true' },
          success: (res) => {
            if (res.statusCode === 200 && res.tempFilePath) {
              this.displaySrc = res.tempFilePath
            } else {
              this.displaySrc = resolved
            }
          },
          fail: () => {
            this.displaySrc = resolved
          }
        })
      } else {
        this.displaySrc = resolved
      }
    }
  }
}
</script>

<style scoped>
</style>
