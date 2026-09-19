<template>
  <view class="page">
    <view class="seg">
      <view
        class="seg-item"
        :class="{ active: currentTab === 'my' }"
        @click="switchTab('my')"
      >我的点单</view>
      <view
        class="seg-item"
        :class="{ active: currentTab === 'making' }"
        @click="switchTab('making')"
      >我的制作</view>
    </view>

    <view v-if="currentTab === 'my'" class="list">
      <view
        class="order-card"
        v-for="order in myOrders"
        :key="order.id"
        @click="goToDetail(order.id)"
      >
        <view class="head">
          <text class="no">{{ order.orderNo }}</text>
          <StatusBadge :status="order.status" />
        </view>
        <view class="dishes">
          <text
            v-for="detail in order.details"
            :key="detail.id"
            class="dish"
          >{{ detail.dishName }} ×{{ detail.quantity }}</text>
        </view>
        <view class="foot">
          <text class="time">{{ formatTime(order.createTime) }}</text>
          <text class="amount">¥{{ order.totalAmount }}</text>
        </view>
        <view v-if="order.status === 0" class="actions" @click.stop>
          <button class="btn btn-sm btn-danger-outline" @click="handleCancel(order)">取消</button>
        </view>
      </view>
      <EmptyState v-if="myOrders.length === 0" icon="order" text="暂无点单" />
    </view>

    <view v-if="currentTab === 'making'" class="list">
      <view
        class="order-card"
        v-for="order in makingOrders"
        :key="order.id"
        @click="goToDetail(order.id)"
      >
        <view class="head">
          <text class="no">{{ order.orderNo }}</text>
          <StatusBadge :status="order.status" />
        </view>
        <text class="customer">来自 {{ order.customerName || '未知' }}</text>
        <view class="dishes">
          <text
            v-for="detail in order.details"
            :key="detail.id"
            class="dish"
          >{{ detail.dishName }} ×{{ detail.quantity }}</text>
        </view>
        <view class="foot">
          <text class="time">{{ formatTime(order.createTime) }}</text>
          <text class="amount">¥{{ order.totalAmount }}</text>
        </view>
        <view class="actions" @click.stop>
          <button
            v-if="order.status === 0"
            class="btn btn-sm btn-primary"
            @click="handleAccept(order)"
          >接单</button>
          <button
            v-if="order.status === 0"
            class="btn btn-sm btn-danger-outline"
            @click="handleReject(order)"
          >拒绝</button>
          <button
            v-if="order.status === 1"
            class="btn btn-sm btn-secondary"
            @click="handleFinish(order)"
          >完成</button>
        </view>
      </view>
      <EmptyState v-if="makingOrders.length === 0" icon="dish" text="暂无制作订单" />
    </view>
  </view>
</template>

<script>
import { orderApi } from '@/utils/api.js'
import StatusBadge from '@/components/StatusBadge.vue'
import EmptyState from '@/components/EmptyState.vue'

export default {
  components: { StatusBadge, EmptyState },
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
        if (this.currentTab === 'my') {
          this.myOrders = await orderApi.getMyOrders()
        } else {
          this.makingOrders = await orderApi.getMyMakingOrders()
        }
      } catch (e) {
        console.error('加载订单失败', e)
      } finally {
        uni.hideLoading()
      }
    },

    formatTime(timeStr) {
      if (timeStr && timeStr.length >= 16) return timeStr.substring(0, 16)
      return timeStr
    },

    goToDetail(orderId) {
      uni.navigateTo({ url: `/pages/order/detail?id=${orderId}` })
    },

    async handleAccept(order) {
      try {
        await orderApi.acceptOrder(order.id)
        uni.showToast({ title: '接单成功', icon: 'success' })
        this.loadOrders()
      } catch (e) {
        uni.showToast({ title: e.message || '接单失败', icon: 'none' })
      }
    },

    async handleReject(order) {
      uni.showModal({
        title: '提示',
        content: '确定要拒绝该订单吗？拒绝后将退还下单人余额。',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.rejectOrder(order.id)
              uni.showToast({ title: '已拒绝订单', icon: 'success' })
              this.loadOrders()
            } catch (e) {
              uni.showToast({ title: e.message || '拒绝失败', icon: 'none' })
            }
          }
        }
      })
    },

    async handleFinish(order) {
      try {
        await orderApi.finishOrder(order.id)
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
              await orderApi.cancelOrder(order.id)
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
.page {
  min-height: 100%;
  background: #F7F3EE;
  box-sizing: border-box;
}

.seg {
  display: flex;
  margin: 16rpx 24rpx 20rpx;
  padding: 6rpx;
  background: #EFE8DF;
  border-radius: 14rpx;
}

.seg-item {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  font-size: 28rpx;
  color: #9A9086;
  border-radius: 10rpx;
  font-weight: 500;
  line-height: 1.2;
}

.seg-item.active {
  background: #FFFFFF;
  color: #B85C38;
  font-weight: 600;
  box-shadow: 0 2rpx 10rpx rgba(42, 36, 32, 0.08);
}

.list {
  padding: 4rpx 24rpx 40rpx;
}

.order-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.no {
  font-size: 24rpx;
  color: #9A9086;
}

.customer {
  display: block;
  font-size: 24rpx;
  color: #6B6158;
  margin-bottom: 12rpx;
}

.dishes {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.dish {
  font-size: 28rpx;
  color: #2A2420;
}

.foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 1rpx solid #E8E0D6;
}

.time {
  font-size: 24rpx;
  color: #9A9086;
}

.amount {
  font-size: 30rpx;
  font-weight: 700;
  color: #B85C38;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  margin-top: 20rpx;
}
</style>
