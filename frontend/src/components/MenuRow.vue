<template>
  <view class="menu-row" @click="$emit('click')">
    <view class="menu-left">
      <view v-if="icon" class="menu-icon">
        <AppIcon :name="icon" :size="34" :color="iconColor" />
      </view>
      <view class="menu-texts">
        <text class="menu-text">{{ title }}</text>
        <text v-if="subtitle" class="menu-sub">{{ subtitle }}</text>
      </view>
    </view>
    <view class="menu-right">
      <text v-if="value" class="menu-value">{{ value }}</text>
      <AppIcon v-if="arrow" name="chevron" :size="24" color="#9A9086" />
    </view>
  </view>
</template>

<script>
import AppIcon from './AppIcon.vue'

export default {
  name: 'MenuRow',
  components: { AppIcon },
  props: {
    title: { type: String, required: true },
    subtitle: { type: String, default: '' },
    value: { type: String, default: '' },
    icon: { type: String, default: '' },
    iconColor: { type: String, default: '#B85C38' },
    arrow: { type: Boolean, default: true }
  },
  emits: ['click']
}
</script>

<style scoped>
.menu-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 8rpx;
  border-bottom: 1rpx solid var(--color-border, #E8E0D6);
}
.menu-row:last-child {
  border-bottom: none;
}
.menu-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
  min-width: 0;
}
.menu-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  background: var(--color-primary-soft, rgba(184, 92, 56, 0.12));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.menu-texts {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 0;
}
.menu-text {
  font-size: 28rpx;
  color: var(--color-text, #2A2420);
  font-weight: 500;
}
.menu-sub {
  font-size: 22rpx;
  color: var(--color-text-muted, #9A9086);
}
.menu-right {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}
.menu-value {
  font-size: 26rpx;
  color: var(--color-text-muted, #9A9086);
}
</style>
