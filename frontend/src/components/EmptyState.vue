<template>
  <view class="empty-state">
    <view class="empty-icon-wrap">
      <AppIcon :name="icon" :size="88" color="#9A9086" />
    </view>
    <text class="empty-title" v-if="title">{{ title }}</text>
    <text class="empty-desc">{{ text }}</text>
    <button v-if="actionText" class="btn btn-primary btn-sm empty-action" @click="$emit('action')">
      {{ actionText }}
    </button>
  </view>
</template>

<script>
import AppIcon from './AppIcon.vue'

export default {
  name: 'EmptyState',
  components: { AppIcon },
  props: {
    icon: { type: String, default: 'empty-plate' },
    title: { type: String, default: '' },
    text: { type: String, default: '暂无内容' },
    actionText: { type: String, default: '' }
  },
  emits: ['action']
}
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 48rpx;
}
.empty-icon-wrap {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: var(--color-bg, #F7F3EE);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
}
.empty-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--color-text, #2A2420);
  margin-bottom: 8rpx;
}
.empty-desc {
  font-size: 26rpx;
  color: var(--color-text-muted, #9A9086);
  text-align: center;
  line-height: 1.5;
}
.empty-action {
  margin-top: 32rpx;
}
</style>
