<template>
  <view class="container">
    <!-- 状态筛选 -->
    <view class="status-filter">
      <view 
        class="filter-item" 
        :class="{ active: currentStatus === null }"
        @click="filterByStatus(null)"
      >
        全部
      </view>
      <view 
        class="filter-item" 
        :class="{ active: currentStatus === 0 }"
        @click="filterByStatus(0)"
      >
        待确认
      </view>
      <view 
        class="filter-item" 
        :class="{ active: currentStatus === 1 }"
        @click="filterByStatus(1)"
      >
        已确认
      </view>
      <view 
        class="filter-item" 
        :class="{ active: currentStatus === 2 }"
        @click="filterByStatus(2)"
      >
        已完成
      </view>
    </view>
    
    <!-- 订单列表 -->
    <view v-if="orders.length > 0" class="order-list">
      <view 
        class="order-item card" 
        v-for="order in orders" 
        :key="order.id"
        @click="goToDetail(order.id)"
      >
        <!-- 订单头部 -->
        <view class="order-header">
          <text class="order-no">订单号: {{ order.orderNo }}</text>
          <view 
            class="order-status badge"
            :class="getStatusClass(order.status)"
          >
            {{ order.statusText }}
          </view>
        </view>
        
        <!-- 订单商品 -->
        <view class="order-dishes">
          <view 
            class="dish-item" 
            v-for="detail in order.details" 
            :key="detail.dishId"
          >
            <SafeImage 
              v-if="detail.dishImage" 
              :src="detail.dishImage" 
              mode="aspectFill"
              imgClass="dish-image"
            />
            <view v-else class="dish-image placeholder-image">
              <text>无图</text>
            </view>
            <view class="dish-info">
              <text class="dish-name">{{ detail.dishName }}</text>
              <text class="dish-price">¥{{ detail.dishPrice }} x {{ detail.quantity }}</text>
            </view>
          </view>
        </view>
        
        <!-- 订单底部 -->
        <view class="order-footer">
          <text class="order-time">{{ formatTime(order.createTime) }}</text>
          <text class="order-total">合计: <text class="price">¥{{ order.totalAmount }}</text></text>
        </view>
        
      </view>
    </view>
    
    <!-- 空状态 -->
    <view v-else class="empty-state">
      <text>暂无订单</text>
    </view>
  </view>
</template>

<script>
import { orderApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'
import SafeImage from '@/components/SafeImage.vue'

export default {
  components: { SafeImage },
  data() {
    return {
      orders: [],
      currentStatus: null
    }
  },
  
  onLoad() {
    // 监听用户信息更新事件
    uni.$on('userInfoUpdated', this.onUserInfoUpdated)
  },
  
  onUnload() {
    // 移除事件监听
    uni.$off('userInfoUpdated', this.onUserInfoUpdated)
  },
  
  onShow() {
    // 每次显示时刷新订单列表
    this.loadOrders()
  },
  
  methods: {
    // 用户信息更新后的回调
    onUserInfoUpdated(userInfo) {
      console.log('订单页接收到用户信息更新事件:', userInfo)
    },
    
    // 加载订单列表
    async loadOrders() {
      try {
        uni.showLoading({ title: '加载中...' })
        const orders = await orderApi.getList(this.currentStatus)
        // 处理状态文本
        this.orders = orders.map(order => ({
          ...order,
          statusText: this.getStatusText(order.status)
        }))
      } catch (error) {
        console.error('加载订单失败:', error)
      } finally {
        uni.hideLoading()
      }
    },
    
    // 按状态筛选
    filterByStatus(status) {
      this.currentStatus = status
      this.loadOrders()
    },
    
    // 跳转到详情页
    goToDetail(orderId) {
      uni.navigateTo({
        url: `/pages/order/detail?id=${orderId}`
      })
    },
    
    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        0: '待确认',
        1: '已确认',
        2: '已完成'
      }
      return statusMap[status] || '未知状态'
    },
    
    // 获取状态样式
    getStatusClass(status) {
      const classMap = {
        0: 'badge-warning',
        1: 'badge-info',
        2: 'badge-success'
      }
      return classMap[status] || ''
    },
    
    // 格式化时间
    formatTime(timeStr) {     
      // 后端已经返回正确格式的时间字符串，直接截取显示部分
      if (timeStr && timeStr.length >= 16) {
        const result = timeStr.substring(0, 16) // 取前16位：yyyy-MM-dd HH:mm
        return result
      }
      return timeStr
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  padding: 20rpx;
}

.status-filter {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
  padding: 0 10rpx;
}

.filter-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  background-color: #F6F3EF;
  border-radius: 999rpx;
  font-size: 26rpx;
  color: #6A625B;
  transition: all 0.3s;
}

.filter-item.active {
  background-color: #7B5B44;
  color: #ffffff;
  font-weight: bold;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.order-item {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(123, 91, 68, 0.06);
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-no {
  font-size: 24rpx;
  color: #A39A92;
}

.order-dishes {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.dish-item {
  display: flex;
  gap: 16rpx;
  align-items: center;
}

.dish-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}
:deep(.dish-image) { width: 100rpx; height: 100rpx; border-radius: 12rpx; }

.placeholder-image {
  background-color: #EFE7DD;
  font-size: 20rpx;
  color: #A39A92;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.dish-name {
  font-size: 28rpx;
  color: #2E2A27;
}

.dish-price {
  font-size: 24rpx;
  color: #6A625B;
}

.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12rpx;
  border-top: 1rpx solid #EFE7DD;
}

.order-time {
  font-size: 24rpx;
  color: #A39A92;
}

.order-total {
  font-size: 28rpx;
  color: #2E2A27;
}

.order-actions {
  display: flex;
  gap: 20rpx;
  justify-content: flex-end;
}

.action-btn {
  padding: 16rpx 32rpx;
  font-size: 26rpx;
  border-radius: 999rpx;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
  color: #999999;
  font-size: 28rpx;
}
</style>
