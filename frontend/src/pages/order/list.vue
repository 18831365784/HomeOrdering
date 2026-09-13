<template>
  <view class="container">
    <!-- Tab切换 -->
    <view class="tab-bar">
      <view
        class="tab-item"
        :class="{ active: currentTab === 'my' }"
        @click="switchTab('my')"
      >
        我的点单
      </view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 'making' }"
        @click="switchTab('making')"
      >
        我的制作
      </view>
    </view>

    <!-- 我的点单 -->
    <view v-if="currentTab === 'my'" class="order-list">
      <view v-if="myOrders.length > 0">
        <view
          class="order-item card"
          v-for="order in myOrders"
          :key="order.id"
          @click="goToDetail(order.id)"
        >
          <view class="order-header">
            <text class="order-no">订单号: {{ order.orderNo }}</text>
            <view class="badge" :class="getBadgeClass(order.status)">
              {{ order.statusText }}
            </view>
          </view>

          <view class="order-dishes">
            <view v-for="detail in order.details" :key="detail.id" class="dish-item">
              <text class="dish-name">{{ detail.dishName }} x{{ detail.quantity }}</text>
            </view>
          </view>

          <view class="order-footer">
            <text class="order-time">{{ formatTime(order.createTime) }}</text>
            <text class="order-total">合计: <text class="price">¥{{ order.totalAmount }}</text></text>
          </view>

          <view v-if="order.status === 0" class="order-actions">
            <button class="btn btn-sm btn-danger" @click.stop="handleCancel(order)">取消</button>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无订单</text>
      </view>
    </view>

    <!-- 我的制作 -->
    <view v-if="currentTab === 'making'" class="order-list">
      <view v-if="makingOrders.length > 0">
        <view
          class="order-item card"
          v-for="order in makingOrders"
          :key="order.id"
          @click="goToDetail(order.id)"
        >
          <view class="order-header">
            <text class="order-no">订单号: {{ order.orderNo }}</text>
            <view class="badge" :class="getBadgeClass(order.status)">
              {{ order.statusText }}
            </view>
          </view>

          <view class="order-dishes">
            <view class="customer-info">
              <text class="customer-label">客户：</text>
              <text class="customer-name">{{ order.customerName || '未知' }}</text>
            </view>
            <view v-for="detail in order.details" :key="detail.id" class="dish-item">
              <text class="dish-name">{{ detail.dishName }} x{{ detail.quantity }}</text>
            </view>
          </view>

          <view class="order-footer">
            <text class="order-time">{{ formatTime(order.createTime) }}</text>
            <text class="order-total">合计: <text class="price">¥{{ order.totalAmount }}</text></text>
          </view>

          <view class="order-actions">
            <button
              v-if="order.status === 0"
              class="btn btn-sm btn-success"
              @click.stop="handleAccept(order)"
            >接单</button>
            <button
              v-if="order.status === 1"
              class="btn btn-sm btn-success"
              @click.stop="handleFinish(order)"
            >完成</button>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">🍳</text>
        <text class="empty-text">暂无制作订单</text>
      </view>
    </view>
  </view>
</template>

<script>
import { orderApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'

export default {
  data() {
    return {
      currentTab: 'my',
      myOrders: [],
      makingOrders: []
    }
  },

  onLoad() {
    this.loadOrders()
  },

  onShow() {
    this.loadOrders()
  },

  methods: {
    switchTab(tab) {
      this.currentTab = tab
      this.loadOrders()
    },

    async loadOrders() {
      try {
        uni.showLoading({ title: '加载中...' })
        const uuid = userManager.getUuid()

        if (this.currentTab === 'my') {
          this.myOrders = await orderApi.getMyOrders(uuid)
        } else {
          this.makingOrders = await orderApi.getMyMakingOrders(uuid)
        }
      } catch (e) {
        console.error('加载订单失败', e)
      } finally {
        uni.hideLoading()
      }
    },

    getBadgeClass(status) {
      const classMap = {
        '-1': 'badge-gray',
        '0': 'badge-warning',
        '1': 'badge-info',
        '2': 'badge-success'
      }
      return classMap[status] || ''
    },

    formatTime(timeStr) {
      if (timeStr && timeStr.length >= 16) {
        return timeStr.substring(0, 16)
      }
      return timeStr
    },

    goToDetail(orderId) {
      uni.navigateTo({
        url: `/pages/order/detail?id=${orderId}`
      })
    },

    async handleAccept(order) {
      try {
        await orderApi.acceptOrder(order.id, userManager.getUuid())
        uni.showToast({ title: '接单成功', icon: 'success' })
        this.loadOrders()
      } catch (e) {
        uni.showToast({ title: e.message || '接单失败', icon: 'none' })
      }
    },

    async handleFinish(order) {
      try {
        await orderApi.finishOrder(order.id, userManager.getUuid())
        uni.showToast({ title: '订单完成', icon: 'success' })
        this.loadOrders()
      } catch (e) {
        uni.showToast({ title: e.message || '操作失败', icon: 'none' })
      }
    },

    async handleCancel(order) {
      uni.showModal({
        title: '提示',
        content: '确定要取消订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.cancelOrder(order.id, userManager.getUuid())
              uni.showToast({ title: '订单已取消', icon: 'success' })
              this.loadOrders()
            } catch (e) {
              uni.showToast({ title: e.message || '取消失败', icon: 'none' })
            }
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #FAFAFA;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
}

/* Tab栏 */
.tab-bar {
  display: flex;
  background: #FFFFFF;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #F5F5F5;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #9E9E9E;
  position: relative;
  transition: all 0.25s ease;
}

.tab-item.active {
  color: #FF6B6B;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80rpx;
  height: 6rpx;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  border-radius: 3rpx;
}

/* 订单列表 */
.order-list {
  padding: 24rpx 32rpx;
}

.order-item {
  margin-bottom: 24rpx;
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.order-no {
  font-size: 24rpx;
  color: #9E9E9E;
}

.order-dishes {
  margin-bottom: 20rpx;
}

.customer-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 12rpx;
  padding-bottom: 12rpx;
  border-bottom: 1rpx dashed #EEEEEE;
}

.customer-label {
  font-size: 24rpx;
  color: #9E9E9E;
}

.customer-name {
  font-size: 28rpx;
  color: #212121;
  font-weight: 500;
}

.dish-item {
  padding: 8rpx 0;
}

.dish-name {
  font-size: 28rpx;
  color: #616161;
}

.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 1rpx solid #F5F5F5;
}

.order-time {
  font-size: 24rpx;
  color: #9E9E9E;
}

.order-total {
  font-size: 28rpx;
  color: #212121;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  margin-top: 20rpx;
  padding-top: 16rpx;
  padding-left: 20rpx;
  border-top: 1rpx solid #F5F5F5;
}

.order-actions .btn {
  padding: 10rpx 60rpx;
  font-size: 24rpx;
  border-radius: 30rpx;
}

/* 徽章 */
.badge {
  display: inline-block;
  padding: 8rpx 20rpx;
  border-radius: 50rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.badge-warning {
  background: rgba(255, 152, 0, 0.15);
  color: #FF9800;
}

.badge-info {
  background: rgba(33, 150, 243, 0.15);
  color: #2196F3;
}

.badge-success {
  background: rgba(76, 175, 80, 0.15);
  color: #4CAF50;
}

.badge-gray {
  background: rgba(158, 158, 158, 0.15);
  color: #9E9E9E;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 0;
}

.empty-icon {
  font-size: 160rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #9E9E9E;
  margin-top: 32rpx;
}
</style>
