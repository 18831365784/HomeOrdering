<template>
  <view class="page">
    <view v-if="order" :class="{ 'page-with-bottom': showActionButton }">
      <view class="status-bar" :class="'tone-' + statusTone">
        <text class="status-label">{{ order.statusText }}</text>
        <text class="order-no">{{ order.orderNo }}</text>
      </view>

      <view class="section">
        <text class="section-title">菜品明细</text>
        <view
          class="dish-row"
          v-for="(detail, index) in order.details"
          :key="detail.dishId + '_' + index"
        >
          <SafeImage
            v-if="detail.dishImage"
            :src="detail.dishImage"
            mode="aspectFill"
            imgClass="dish-img"
          />
          <view v-else class="dish-img placeholder">
            <AppIcon name="dish" :size="32" color="#D4C9BC" />
          </view>
          <view class="dish-info">
            <text class="dish-name">{{ detail.dishName }}</text>
            <view v-if="getDishOptionsByDetail(detail, index)" class="opts">
              <text
                v-for="option in getDishOptionsByDetail(detail, index)"
                :key="option.key"
                class="opt"
              >{{ option.key }}: {{ formatOptionValue(option.value) }}</text>
            </view>
            <text class="dish-meta">¥{{ detail.dishPrice }} × {{ detail.quantity }}</text>
          </view>
          <text class="subtotal">¥{{ detail.subtotal }}</text>
        </view>
      </view>

      <view v-if="order.remark && formatRemark(order.remark).trim()" class="section">
        <text class="section-title">备注</text>
        <text class="remark">{{ formatRemark(order.remark) }}</text>
      </view>

      <view class="section">
        <text class="section-title">订单信息</text>
        <view class="info-row">
          <text class="info-label">创建时间</text>
          <text class="info-value">{{ formatTime(order.createTime) }}</text>
        </view>
        <view class="info-row total">
          <text class="info-label">订单总额</text>
          <text class="price">¥{{ order.totalAmount }}</text>
        </view>
      </view>
    </view>

    <view v-else class="loading">
      <text class="text-muted">加载中...</text>
    </view>

    <view v-if="showActionButton" class="bottom-bar">
      <button
        v-if="isMaker && order.status === 0"
        class="btn btn-primary flex-1"
        @click="handleAccept"
      >接单</button>
      <button
        v-if="isMaker && order.status === 0"
        class="btn btn-danger-outline flex-1"
        @click="handleReject"
      >拒绝</button>
      <button
        v-if="isCustomer && !isMaker && order.status === 0"
        class="btn btn-danger-outline btn-block"
        @click="handleCancel"
      >取消订单</button>
      <button
        v-if="isMaker && order.status === 1"
        class="btn btn-secondary btn-block"
        @click="handleFinish"
      >完成订单</button>
    </view>
  </view>
</template>

<script>
import { orderApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'
import SafeImage from '@/components/SafeImage.vue'
import AppIcon from '@/components/AppIcon.vue'

export default {
  components: { SafeImage, AppIcon },
  data() {
    return {
      orderId: null,
      order: null,
      currentUuid: '',
      isMaker: false,
      isCustomer: false,
      extractedOptions: []
    }
  },

  computed: {
    showActionButton() {
      if (!this.order) return false
      if (this.order.status === 0 && this.isMaker) return true
      if (this.order.status === 0 && this.isCustomer && !this.isMaker) return true
      if (this.order.status === 1 && this.isMaker) return true
      return false
    },
    statusTone() {
      const map = { '-2': 'danger', '-1': 'muted', '0': 'warning', '1': 'primary', '2': 'success' }
      return map[String(this.order && this.order.status)] || 'muted'
    }
  },

  onLoad(options) {
    this.orderId = options.id
    this.currentUuid = userManager.getUuid()
    this.loadOrderDetail()
  },

  methods: {
    checkUserRole() {
      if (!this.order || !this.currentUuid) return
      this.isMaker = this.order.makerUuid === this.currentUuid
      this.isCustomer = this.order.customerUuid === this.currentUuid
    },

    async loadOrderDetail() {
      try {
        uni.showLoading({ title: '加载中...' })
        const order = await orderApi.getDetail(this.orderId)
        this.order = {
          ...order,
          statusText: this.getStatusText(order.status)
        }
        this.checkUserRole()
        this.extractedOptions = this.extractOptionsFromRemark(order.remark)
      } catch (error) {
        console.error('加载订单详情失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },

    handleAccept() {
      uni.showModal({
        title: '提示',
        content: '确定要接单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.acceptOrder(this.orderId)
              uni.showToast({ title: '接单成功', icon: 'success' })
              this.loadOrderDetail()
            } catch (error) {
              uni.showToast({ title: error.message || '接单失败', icon: 'none' })
            }
          }
        }
      })
    },

    handleReject() {
      uni.showModal({
        title: '提示',
        content: '确定要拒绝该订单吗？拒绝后将退还下单人余额。',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.rejectOrder(this.orderId)
              uni.showToast({ title: '已拒绝订单', icon: 'success' })
              this.loadOrderDetail()
            } catch (error) {
              uni.showToast({ title: error.message || '拒绝失败', icon: 'none' })
            }
          }
        }
      })
    },

    handleCancel() {
      uni.showModal({
        title: '提示',
        content: '确定要取消订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.cancelOrder(this.orderId)
              uni.showToast({ title: '订单已取消', icon: 'success' })
              this.loadOrderDetail()
            } catch (error) {
              uni.showToast({ title: error.message || '取消失败', icon: 'none' })
            }
          }
        }
      })
    },

    handleFinish() {
      uni.showModal({
        title: '提示',
        content: '确定完成订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.finishOrder(this.orderId)
              uni.showToast({ title: '订单已完成', icon: 'success' })
              this.loadOrderDetail()
            } catch (error) {
              uni.showToast({ title: error.message || '操作失败', icon: 'none' })
            }
          }
        }
      })
    },

    getStatusText(status) {
      const statusMap = {
        0: '待接单',
        1: '制作中',
        2: '已完成',
        '-1': '已取消',
        '-2': '制作人已拒绝'
      }
      return statusMap[status] || '未知状态'
    },

    formatTime(timeStr) {
      if (timeStr && timeStr.length >= 16) return timeStr.substring(0, 16)
      return timeStr
    },

    formatRemark(remark) {
      if (!remark) return ''
      const lines = remark.split('\n')
      const filteredLines = []
      let skipOptions = false
      for (const line of lines) {
        if (line.includes('【菜品选项】')) {
          skipOptions = true
          continue
        }
        if (skipOptions && line.trim() === '') continue
        if (skipOptions && line.match(/^\s*.+\(\d+份\):$/)) continue
        if (skipOptions && line.match(/^\s*.+:\s*.+$/)) continue
        if (skipOptions && line.trim() === '') {
          skipOptions = false
          continue
        }
        if (!skipOptions) filteredLines.push(line)
      }
      return filteredLines.join('\n').trim()
    },

    getDishOptionsByDetail(detail, detailIndex) {
      const matchedOptions = this.extractedOptions.filter(opt =>
        opt.dishName === detail.dishName && opt.quantity === detail.quantity
      )
      if (matchedOptions.length > 1) {
        const optionIndex = Math.min(detailIndex, matchedOptions.length - 1)
        return matchedOptions[optionIndex].items
      }
      if (matchedOptions.length === 1) return matchedOptions[0].items
      return null
    },

    formatOptionValue(value) {
      if (value === true || value === 'true') return '是'
      if (value === false || value === 'false') return '否'
      if (Array.isArray(value)) return value.join('、')
      return value
    },

    extractOptionsFromRemark(remark) {
      if (!remark) return []
      const options = []
      const lines = remark.split('\n')
      let currentOption = null
      let inOptionsSection = false
      for (const line of lines) {
        if (line.includes('【菜品选项】')) {
          inOptionsSection = true
          continue
        }
        if (inOptionsSection) {
          const dishMatch = line.match(/^(.+)\((\d+)份\):$/)
          if (dishMatch) {
            if (currentOption) options.push(currentOption)
            currentOption = {
              dishName: dishMatch[1].trim(),
              quantity: parseInt(dishMatch[2]),
              items: []
            }
            continue
          }
          const optionMatch = line.match(/^\s+(.+?):\s*(.+)$/)
          if (optionMatch && currentOption) {
            currentOption.items.push({
              key: optionMatch[1].trim(),
              value: optionMatch[2].trim()
            })
          }
        }
      }
      if (currentOption) options.push(currentOption)
      return options
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

.status-bar {
  border-radius: 20rpx;
  padding: 36rpx 28rpx;
  margin-bottom: 20rpx;
  background: #FFFFFF;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
}

.status-label {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #2A2420;
  margin-bottom: 8rpx;
}
.tone-primary .status-label { color: #B85C38; }
.tone-success .status-label { color: #5C6B5A; }
.tone-warning .status-label { color: #C47A2C; }
.tone-danger .status-label { color: #B33A2B; }
.tone-muted .status-label { color: #9A9086; }

.order-no {
  font-size: 24rpx;
  color: #9A9086;
}

.section {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #2A2420;
  margin-bottom: 20rpx;
}

.dish-row {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #E8E0D6;
}

.dish-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.dish-row:first-of-type {
  padding-top: 0;
}

:deep(.dish-img) {
  width: 96rpx;
  height: 96rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.placeholder {
  width: 96rpx;
  height: 96rpx;
  border-radius: 12rpx;
  background: #F7F3EE;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dish-info {
  flex: 1;
  min-width: 0;
}

.dish-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #2A2420;
}

.opts {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  margin-top: 6rpx;
}

.opt {
  font-size: 22rpx;
  color: #B85C38;
}

.dish-meta {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #9A9086;
}

.subtotal {
  font-size: 28rpx;
  font-weight: 600;
  color: #2A2420;
}

.remark {
  font-size: 28rpx;
  color: #6B6158;
  line-height: 1.6;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
}

.info-row.total {
  margin-top: 8rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #E8E0D6;
}

.info-label {
  font-size: 28rpx;
  color: #6B6158;
}

.info-value {
  font-size: 28rpx;
  color: #2A2420;
}

.loading {
  padding: 200rpx 0;
  text-align: center;
}

.flex-1 {
  flex: 1;
}
</style>
