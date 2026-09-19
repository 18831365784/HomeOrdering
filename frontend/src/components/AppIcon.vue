<template>
  <view class="app-icon" :style="wrapStyle">
    <!-- search -->
    <view v-if="name === 'search'" class="ico">
      <view class="ring" :style="strokeStyle"></view>
      <view class="handle" :style="fillStyle"></view>
    </view>
    <!-- plus -->
    <view v-else-if="name === 'plus'" class="ico">
      <view class="bar-h" :style="fillStyle"></view>
      <view class="bar-v" :style="fillStyle"></view>
    </view>
    <!-- minus -->
    <view v-else-if="name === 'minus'" class="ico">
      <view class="bar-h" :style="fillStyle"></view>
    </view>
    <!-- chevron -->
    <view v-else-if="name === 'chevron'" class="ico chevron" :style="{ borderColor: color }"></view>
    <!-- edit -->
    <view v-else-if="name === 'edit'" class="ico">
      <view class="pencil" :style="{ borderColor: color, background: color }"></view>
    </view>
    <!-- close -->
    <view v-else-if="name === 'close'" class="ico">
      <view class="x1" :style="fillStyle"></view>
      <view class="x2" :style="fillStyle"></view>
    </view>
    <!-- empty-plate -->
    <view v-else-if="name === 'empty-plate'" class="ico plate">
      <view class="plate-outer" :style="strokeStyle"></view>
      <view class="plate-inner" :style="strokeStyle"></view>
    </view>
    <!-- settings -->
    <view v-else-if="name === 'settings'" class="ico">
      <view class="gear" :style="strokeStyle"></view>
      <view class="gear-dot" :style="fillStyle"></view>
    </view>
    <!-- dish -->
    <view v-else-if="name === 'dish'" class="ico">
      <view class="bowl" :style="strokeStyle"></view>
      <view class="steam s1" :style="fillStyle"></view>
      <view class="steam s2" :style="fillStyle"></view>
    </view>
    <!-- tag -->
    <view v-else-if="name === 'tag'" class="ico">
      <view class="tag-shape" :style="strokeStyle"></view>
      <view class="tag-dot" :style="fillStyle"></view>
    </view>
    <!-- order -->
    <view v-else-if="name === 'order'" class="ico">
      <view class="doc" :style="strokeStyle"></view>
      <view class="doc-line l1" :style="fillStyle"></view>
      <view class="doc-line l2" :style="fillStyle"></view>
      <view class="doc-line l3" :style="fillStyle"></view>
    </view>
    <!-- family -->
    <view v-else-if="name === 'family'" class="ico">
      <view class="home-roof" :style="{ borderBottomColor: color }"></view>
      <view class="home-body" :style="strokeStyle"></view>
    </view>
    <!-- check -->
    <view v-else-if="name === 'check'" class="ico">
      <view class="check-mark" :style="{ borderColor: color }"></view>
    </view>
    <!-- camera -->
    <view v-else-if="name === 'camera'" class="ico">
      <view class="cam-body" :style="strokeStyle"></view>
      <view class="cam-lens" :style="strokeStyle"></view>
    </view>
    <!-- drag -->
    <view v-else-if="name === 'drag'" class="ico drag">
      <view class="dot" :style="fillStyle"></view>
      <view class="dot" :style="fillStyle"></view>
      <view class="dot" :style="fillStyle"></view>
      <view class="dot" :style="fillStyle"></view>
      <view class="dot" :style="fillStyle"></view>
      <view class="dot" :style="fillStyle"></view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'AppIcon',
  props: {
    name: { type: String, required: true },
    size: { type: [Number, String], default: 36 },
    color: { type: String, default: '#6B6158' }
  },
  computed: {
    wrapStyle() {
      const s = Number(this.size)
      return { width: s + 'rpx', height: s + 'rpx' }
    },
    strokeStyle() {
      return { borderColor: this.color }
    },
    fillStyle() {
      return { backgroundColor: this.color }
    }
  }
}
</script>

<style scoped>
.app-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
}
.ico {
  width: 100%;
  height: 100%;
  position: relative;
}
.ring {
  position: absolute;
  left: 12%;
  top: 10%;
  width: 58%;
  height: 58%;
  border: 3rpx solid;
  border-radius: 50%;
  box-sizing: border-box;
}
.handle {
  position: absolute;
  width: 28%;
  height: 3rpx;
  right: 10%;
  bottom: 22%;
  transform: rotate(45deg);
  border-radius: 2rpx;
}
.bar-h {
  position: absolute;
  left: 18%;
  right: 18%;
  top: 50%;
  height: 3rpx;
  margin-top: -1.5rpx;
  border-radius: 2rpx;
}
.bar-v {
  position: absolute;
  top: 18%;
  bottom: 18%;
  left: 50%;
  width: 3rpx;
  margin-left: -1.5rpx;
  border-radius: 2rpx;
}
.chevron {
  width: 36%;
  height: 36%;
  border-right: 3rpx solid;
  border-bottom: 3rpx solid;
  transform: rotate(-45deg);
  margin-left: -8%;
}
.pencil {
  position: absolute;
  left: 28%;
  top: 18%;
  width: 44%;
  height: 44%;
  border: 3rpx solid;
  transform: rotate(45deg);
  border-radius: 4rpx;
}
.pencil::after {
  content: '';
  position: absolute;
  bottom: -18%;
  left: 30%;
  width: 40%;
  height: 28%;
  background: inherit;
  clip-path: polygon(0 0, 100% 0, 50% 100%);
}
.x1, .x2 {
  position: absolute;
  left: 18%;
  right: 18%;
  top: 50%;
  height: 3rpx;
  margin-top: -1.5rpx;
  border-radius: 2rpx;
}
.x1 { transform: rotate(45deg); }
.x2 { transform: rotate(-45deg); }
.plate-outer {
  position: absolute;
  left: 10%;
  top: 18%;
  width: 80%;
  height: 64%;
  border: 3rpx solid;
  border-radius: 50%;
  box-sizing: border-box;
}
.plate-inner {
  position: absolute;
  left: 26%;
  top: 32%;
  width: 48%;
  height: 36%;
  border: 3rpx solid;
  border-radius: 50%;
  box-sizing: border-box;
}
.gear {
  position: absolute;
  left: 22%;
  top: 22%;
  width: 56%;
  height: 56%;
  border: 3rpx solid;
  border-radius: 50%;
  box-sizing: border-box;
}
.gear-dot {
  position: absolute;
  left: 42%;
  top: 42%;
  width: 16%;
  height: 16%;
  border-radius: 50%;
}
.bowl {
  position: absolute;
  left: 14%;
  top: 42%;
  width: 72%;
  height: 36%;
  border: 3rpx solid;
  border-top: none;
  border-radius: 0 0 50% 50%;
  box-sizing: border-box;
}
.steam {
  position: absolute;
  width: 3rpx;
  height: 18%;
  border-radius: 2rpx;
  top: 18%;
}
.s1 { left: 38%; }
.s2 { left: 58%; top: 14%; }
.tag-shape {
  position: absolute;
  left: 18%;
  top: 22%;
  width: 52%;
  height: 56%;
  border: 3rpx solid;
  border-radius: 6rpx 6rpx 6rpx 20rpx;
  transform: rotate(-35deg);
  box-sizing: border-box;
}
.tag-dot {
  position: absolute;
  left: 28%;
  top: 38%;
  width: 10%;
  height: 10%;
  border-radius: 50%;
}
.doc {
  position: absolute;
  left: 22%;
  top: 12%;
  width: 56%;
  height: 76%;
  border: 3rpx solid;
  border-radius: 6rpx;
  box-sizing: border-box;
}
.doc-line {
  position: absolute;
  left: 34%;
  height: 3rpx;
  border-radius: 2rpx;
}
.l1 { top: 32%; width: 32%; }
.l2 { top: 48%; width: 32%; }
.l3 { top: 64%; width: 22%; }
.home-roof {
  position: absolute;
  left: 50%;
  top: 12%;
  width: 0;
  height: 0;
  margin-left: -28%;
  border-left: 28% solid transparent;
  border-right: 28% solid transparent;
  border-bottom: 28% solid;
}
.home-body {
  position: absolute;
  left: 26%;
  top: 40%;
  width: 48%;
  height: 42%;
  border: 3rpx solid;
  border-top: none;
  box-sizing: border-box;
}
.check-mark {
  position: absolute;
  left: 22%;
  top: 28%;
  width: 42%;
  height: 24%;
  border-left: 3rpx solid;
  border-bottom: 3rpx solid;
  transform: rotate(-45deg);
}
.cam-body {
  position: absolute;
  left: 12%;
  top: 28%;
  width: 76%;
  height: 48%;
  border: 3rpx solid;
  border-radius: 8rpx;
  box-sizing: border-box;
}
.cam-lens {
  position: absolute;
  left: 36%;
  top: 36%;
  width: 28%;
  height: 28%;
  border: 3rpx solid;
  border-radius: 50%;
  box-sizing: border-box;
}
.drag {
  display: flex;
  flex-wrap: wrap;
  align-content: space-between;
  justify-content: space-between;
  padding: 18%;
  box-sizing: border-box;
}
.drag .dot {
  width: 28%;
  height: 28%;
  border-radius: 50%;
}
</style>
