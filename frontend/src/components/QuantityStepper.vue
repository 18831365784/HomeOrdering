<template>
  <view class="stepper">
    <view class="step-btn" :class="{ disabled: value <= min }" @click="dec">
      <AppIcon name="minus" :size="28" :color="value <= min ? '#D4C9BC' : '#B85C38'" />
    </view>
    <text class="step-value">{{ value }}</text>
    <view class="step-btn plus" :class="{ disabled: value >= max }" @click="inc">
      <AppIcon name="plus" :size="28" :color="value >= max ? '#D4C9BC' : '#FFFFFF'" />
    </view>
  </view>
</template>

<script>
import AppIcon from './AppIcon.vue'

export default {
  name: 'QuantityStepper',
  components: { AppIcon },
  props: {
    value: { type: Number, default: 1 },
    min: { type: Number, default: 1 },
    max: { type: Number, default: 99 }
  },
  emits: ['update:value', 'change'],
  methods: {
    dec() {
      if (this.value <= this.min) return
      const next = this.value - 1
      this.$emit('update:value', next)
      this.$emit('change', next)
    },
    inc() {
      if (this.value >= this.max) return
      const next = this.value + 1
      this.$emit('update:value', next)
      this.$emit('change', next)
    }
  }
}
</script>

<style scoped>
.stepper {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
}
.step-btn {
  width: 52rpx;
  height: 52rpx;
  border-radius: 12rpx;
  background: var(--color-bg, #F7F3EE);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1rpx solid var(--color-border, #E8E0D6);
}
.step-btn.plus {
  background: var(--color-primary, #B85C38);
  border-color: var(--color-primary, #B85C38);
}
.step-btn.disabled {
  opacity: 0.5;
}
.step-value {
  min-width: 56rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 600;
  color: var(--color-text, #2A2420);
}
</style>
