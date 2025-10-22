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
        等待确认
      </view>
      <view 
        class="filter-item" 
        :class="{ active: currentStatus === 1 }"
        @click="filterByStatus(1)"
      >
        已许可
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
            <image 
              v-if="detail.dishImage" 
              :src="detail.dishImage" 
              mode="aspectFill"
              class="dish-image"
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
        
        <!-- 订单操作 -->
        <view class="order-actions" @click.stop>
          <button 
            v-if="order.status === 0" 
            class="action-btn btn-primary primary-bg"
            @click="confirmOrder(order.id)"
          >
            老公确认
          </button>
          <button 
            v-if="order.status === 1" 
            class="action-btn btn-primary primary-bg"
            @click="completeOrder(order.id)"
          >
            完成订单
          </button>
          <button 
            class="action-btn btn-secondary"
            @click="deleteOrder(order.id)"
          >
            删除
          </button>
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

export default {
  data() {
    return {
      orders: [],
      currentStatus: null
    }
  },
  
  onShow() {
    this.loadOrders()
  },
  
  methods: {
    // 加载订单列表
    async loadOrders() {
      try {
        uni.showLoading({ title: '加载中...' })
        const orders = await orderApi.getList(this.currentStatus)
        this.orders = orders
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
    
    // 确认订单
    confirmOrder(orderId) {
      uni.showModal({
        title: '老公大人',
        content: '确认许可这个订单吗？❤️',
        confirmText: '许可',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.updateStatus(orderId, 1)
              uni.showToast({
                title: '已许可',
                icon: 'success'
              })
              this.loadOrders()
            } catch (error) {
              console.error('更新订单状态失败:', error)
            }
          }
        }
      })
    },
    
    // 完成订单
    completeOrder(orderId) {
      uni.showModal({
        title: '提示',
        content: '确认完成这个订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.updateStatus(orderId, 2)
              uni.showToast({
                title: '订单已完成',
                icon: 'success'
              })
              this.loadOrders()
            } catch (error) {
              console.error('更新订单状态失败:', error)
            }
          }
        }
      })
    },
    
    // 删除订单
    deleteOrder(orderId) {
      uni.showModal({
        title: '提示',
        content: '确定要删除这个订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.delete(orderId)
              uni.showToast({
                title: '已删除',
                icon: 'success'
              })
              this.loadOrders()
            } catch (error) {
              console.error('删除订单失败:', error)
            }
          }
        }
      })
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
      const date = new Date(timeStr)
      const month = date.getMonth() + 1
      const day = date.getDate()
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      return `${month}-${day} ${hour}:${minute}`
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
  background-color: #f0f0f0;
  border-radius: 8rpx;
  font-size: 26rpx;
  color: #666666;
  transition: all 0.3s;
}

.filter-item.active {
  background-color: #FF6B6B;
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
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-no {
  font-size: 24rpx;
  color: #999999;
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
  border-radius: 8rpx;
  flex-shrink: 0;
}

.placeholder-image {
  background-color: #f0f0f0;
  font-size: 20rpx;
  color: #999999;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.dish-name {
  font-size: 28rpx;
  color: #333333;
}

.dish-price {
  font-size: 24rpx;
  color: #999999;
}

.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12rpx;
  border-top: 1rpx solid #f0f0f0;
}

.order-time {
  font-size: 24rpx;
  color: #999999;
}

.order-total {
  font-size: 28rpx;
  color: #333333;
}

.order-actions {
  display: flex;
  gap: 20rpx;
  justify-content: flex-end;
}

.action-btn {
  padding: 16rpx 32rpx;
  font-size: 26rpx;
  border-radius: 8rpx;
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
