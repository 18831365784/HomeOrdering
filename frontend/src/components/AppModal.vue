<template>
  <view v-if="show" class="modal-mask" @click="onMask">
    <view class="modal-panel" @click.stop>
      <view class="modal-header" v-if="title || showClose">
        <text class="modal-title">{{ title }}</text>
        <view v-if="showClose" class="modal-close" @click="close">
          <AppIcon name="close" :size="28" color="#9A9086" />
        </view>
      </view>
      <view class="modal-body">
        <slot></slot>
      </view>
      <view class="modal-footer" v-if="showFooter">
        <slot name="footer">
          <button v-if="cancelText" class="btn btn-ghost footer-btn" @click="close">{{ cancelText }}</button>
          <button v-if="confirmText" class="btn btn-primary footer-btn" :loading="loading" @click="$emit('confirm')">
            {{ confirmText }}
          </button>
        </slot>
      </view>
    </view>
  </view>
</template>

<script>
import AppIcon from './AppIcon.vue'

export default {
  name: 'AppModal',
  components: { AppIcon },
  props: {
    show: { type: Boolean, default: false },
    title: { type: String, default: '' },
    showClose: { type: Boolean, default: true },
    showFooter: { type: Boolean, default: true },
    cancelText: { type: String, default: '取消' },
    confirmText: { type: String, default: '确定' },
    loading: { type: Boolean, default: false },
    closeOnMask: { type: Boolean, default: true }
  },
  emits: ['update:show', 'confirm', 'close'],
  methods: {
    close() {
      this.$emit('update:show', false)
      this.$emit('close')
    },
    onMask() {
      if (this.closeOnMask) this.close()
    }
  }
}
</script>

<style scoped>
.modal-mask {
  position: fixed;
  inset: 0;
  padding-top: 200rpx;
  background: rgba(42, 36, 32, 0.48);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  box-sizing: border-box;
}
.modal-panel {
  width: 100%;
  max-height: 100%;
  background: #FFFFFF;
  border-radius: 28rpx 28rpx 0 0;
  padding: 36rpx 36rpx calc(36rpx + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #E8E0D6;
  flex-shrink: 0;
}
.modal-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #2A2420;
  line-height: 1.3;
}
.modal-close {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #F7F3EE;
  flex-shrink: 0;
}
.modal-body {
  overflow-y: auto;
  max-height: 55vh;
  padding-bottom: 8rpx;
}
.modal-footer {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
  flex-shrink: 0;
}
.footer-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  padding: 0;
}
</style>
