<template>
  <view class="page">
    <view class="panel">
      <text class="hint">遇到用着不顺的地方，写下来就好。登录后即可提交。</text>
      <textarea
        class="editor"
        v-model="content"
        maxlength="1000"
        placeholder="请描述遇到的问题"
        :disabled="submitting"
      />
      <text class="count">{{ content.length }}/1000</text>
    </view>
    <button class="btn btn-primary btn-block" :loading="submitting" @click="submit">提交</button>
  </view>
</template>

<script>
import { feedbackApi } from '@/utils/api.js'

export default {
  data() {
    return {
      content: '',
      submitting: false
    }
  },
  methods: {
    async submit() {
      const text = (this.content || '').trim()
      if (!text) {
        uni.showToast({ title: '请填写问题描述', icon: 'none' })
        return
      }
      if (this.submitting) return
      this.submitting = true
      try {
        await feedbackApi.submit(text)
        uni.showToast({ title: '已收到反馈', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 500)
      } catch (e) {
        // 请求失败时 api.js 已提示
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100%;
  background: #F7F3EE;
  padding: 24rpx;
  box-sizing: border-box;
}

.panel {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 32rpx;
}

.hint {
  display: block;
  font-size: 26rpx;
  color: #6B6158;
  line-height: 1.6;
  margin-bottom: 20rpx;
}

.editor {
  width: 100%;
  min-height: 320rpx;
  background: #F7F3EE;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  box-sizing: border-box;
  font-size: 28rpx;
  color: #2A2420;
  line-height: 1.6;
}

.count {
  display: block;
  margin-top: 12rpx;
  text-align: right;
  font-size: 22rpx;
  color: #9A9086;
}
</style>
