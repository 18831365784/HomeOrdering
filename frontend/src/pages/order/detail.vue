<template>
  <view class="container">
    <view v-if="order" class="order-detail">
      <!-- 订单状态卡片 -->
      <view class="status-card card">
        <view class="status-icon">
          <text v-if="order.status === 0">⏰</text>
          <text v-else-if="order.status === 1">💕</text>
          <text v-else>✅</text>
        </view>
        <view class="status-info">
          <text class="status-text">{{ order.statusText }}</text>
          <text class="order-no">订单号: {{ order.orderNo }}</text>
        </view>
      </view>
      
      <!-- 菜品列表 -->
      <view class="dishes-card card">
        <text class="card-title">菜品明细</text>
        <view class="dish-list">
          <view class="dish-item" v-for="detail in order.details" :key="detail.dishId">
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
              <text class="dish-meta">¥{{ detail.dishPrice }} x {{ detail.quantity }}</text>
            </view>
            <text class="dish-subtotal price-small">¥{{ detail.subtotal }}</text>
          </view>
        </view>
      </view>
      
      <!-- 备注信息 -->
      <view v-if="order.remark" class="remark-card card">
        <text class="card-title">备注信息</text>
        <text class="remark-content">{{ formatRemark(order.remark) }}</text>
      </view>
      
      <!-- 扩展选项信息 -->
      <view v-if="extractedOptions.length > 0" class="options-card card">
        <text class="card-title">菜品选项</text>
        <view class="options-list">
          <view v-for="option in extractedOptions" :key="option.dishName" class="option-group">
            <text class="dish-name">{{ option.dishName }}({{ option.quantity }}份)</text>
            <view class="option-items">
              <view v-for="item in option.items" :key="item.key" class="option-item">
                <text class="option-label">{{ item.key }}:</text>
                <text class="option-value">{{ item.value }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 订单信息 -->
      <view class="info-card card">
        <text class="card-title">订单信息</text>
        <view class="info-list">
          <view class="info-item">
            <text class="info-label">创建时间:</text>
            <text class="info-value">{{ formatTime(order.createTime) }}</text>
          </view>
          <view class="info-item total">
            <text class="info-label">订单总额:</text>
            <text class="info-value price">¥{{ order.totalAmount }}</text>
          </view>
        </view>
      </view>
      
      <!-- 操作按钮 -->
      <view class="action-buttons" v-if="isAdmin">
        <button 
          v-if="order.status === 0" 
          class="btn btn-primary primary-bg"
          @click="confirmOrder"
        >
          老公确认
        </button>
        <button 
          v-if="order.status === 1" 
          class="btn btn-primary primary-bg"
          @click="completeOrder"
        >
          完成订单
        </button>
        <button 
          class="btn btn-secondary"
          @click="deleteOrder"
        >
          删除订单
        </button>
      </view>
    </view>
    
    <!-- 加载中 -->
    <view v-else class="loading">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script>
import { orderApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'

export default {
  data() {
    return {
      orderId: null,
      order: null,
      isAdmin: false,
      extractedOptions: []
    }
  },
  
  onLoad(options) {
    this.orderId = options.id
    this.checkAdminStatus()
    this.loadOrderDetail()
  },
  
  methods: {
    // 检查管理员状态
    checkAdminStatus() {
      this.isAdmin = userManager.isAdmin()
      console.log('订单详情页管理员状态:', this.isAdmin)
    },
    
    // 加载订单详情
    async loadOrderDetail() {
      try {
        uni.showLoading({ title: '加载中...' })
        const order = await orderApi.getDetail(this.orderId)
        // 处理状态文本
        this.order = {
          ...order,
          statusText: this.getStatusText(order.status)
        }
        // 提取扩展选项信息
        this.extractedOptions = this.extractOptionsFromRemark(order.remark)
      } catch (error) {
        console.error('加载订单详情失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },
    
    // 确认订单
    confirmOrder() {
      uni.showModal({
        title: '老公大人',
        content: '确认许可这个订单吗？❤️',
        confirmText: '许可',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.updateStatus(this.orderId, 1)
              uni.showToast({
                title: '已许可',
                icon: 'success'
              })
              this.loadOrderDetail()
            } catch (error) {
              console.error('更新订单状态失败:', error)
            }
          }
        }
      })
    },
    
    // 完成订单
    completeOrder() {
      uni.showModal({
        title: '提示',
        content: '确认完成这个订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.updateStatus(this.orderId, 2)
              uni.showToast({
                title: '订单已完成',
                icon: 'success'
              })
              this.loadOrderDetail()
            } catch (error) {
              console.error('更新订单状态失败:', error)
            }
          }
        }
      })
    },
    
    // 删除订单
    deleteOrder() {
      uni.showModal({
        title: '提示',
        content: '确定要删除这个订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.delete(this.orderId)
              uni.showToast({
                title: '已删除',
                icon: 'success'
              })
              setTimeout(() => {
                uni.navigateBack()
              }, 1000)
            } catch (error) {
              console.error('删除订单失败:', error)
            }
          }
        }
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
    
    // 格式化时间
    formatTime(timeStr) {
      console.log('原始时间字符串:', timeStr)
      
      // 后端已经返回正确格式的时间字符串，直接截取显示部分
      if (timeStr && timeStr.length >= 16) {
        const result = timeStr.substring(0, 16) // 取前16位：yyyy-MM-dd HH:mm
        console.log('格式化结果:', result)
        return result
      }
      return timeStr
    },
    
    // 格式化备注，移除扩展选项部分
    formatRemark(remark) {
      if (!remark) return ''
      // 移除【菜品选项】部分，只保留用户原始备注
      const lines = remark.split('\n')
      const filteredLines = []
      let skipOptions = false
      
      for (const line of lines) {
        if (line.includes('【菜品选项】')) {
          skipOptions = true
          continue
        }
        if (skipOptions && line.trim() === '') {
          continue
        }
        if (skipOptions && line.match(/^\s*\w+\(\d+份\):/)) {
          continue
        }
        if (skipOptions && line.match(/^\s*\w+:/)) {
          continue
        }
        if (skipOptions && line.trim() === '') {
          skipOptions = false
          continue
        }
        if (!skipOptions) {
          filteredLines.push(line)
        }
      }
      
      return filteredLines.join('\n').trim()
    },
    
    // 从备注中提取扩展选项信息
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
          // 匹配菜品名称(数量份): 格式
          const dishMatch = line.match(/^(\w+)\((\d+)份\):$/)
          if (dishMatch) {
            if (currentOption) {
              options.push(currentOption)
            }
            currentOption = {
              dishName: dishMatch[1],
              quantity: parseInt(dishMatch[2]),
              items: []
            }
            continue
          }
          
          // 匹配选项: 值 格式
          const optionMatch = line.match(/^\s+(\w+):\s*(.+)$/)
          if (optionMatch && currentOption) {
            currentOption.items.push({
              key: optionMatch[1],
              value: optionMatch[2]
            })
          }
        }
      }
      
      if (currentOption) {
        options.push(currentOption)
      }
      
      return options
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  padding: 20rpx;
  padding-bottom: 160rpx;
}

.status-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(123, 91, 68, 0.06);
}

.status-icon {
  font-size: 80rpx;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.status-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #2E2A27;
}

.order-no {
  font-size: 24rpx;
  color: #A39A92;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #2E2A27;
  margin-bottom: 20rpx;
  display: block;
}

.dishes-card {
  display: flex;
  flex-direction: column;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.dish-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

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
  font-weight: bold;
}

.dish-meta {
  font-size: 24rpx;
  color: #6A625B;
}

.dish-subtotal {
  font-size: 28rpx;
}

.remark-card {
  display: flex;
  flex-direction: column;
}

.remark-content {
  font-size: 28rpx;
  color: #6A625B;
  line-height: 1.6;
}

.info-card {
  display: flex;
  flex-direction: column;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item.total {
  padding-top: 16rpx;
  border-top: 1rpx solid #EFE7DD;
}

.info-label {
  font-size: 28rpx;
  color: #6A625B;
}

.info-value {
  font-size: 28rpx;
  color: #2E2A27;
}

.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 20rpx;
  box-shadow: 0 -8rpx 24rpx rgba(123, 91, 68, 0.06);
  display: flex;
  gap: 20rpx;
}

.action-buttons .btn {
  flex: 1;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
  color: #999999;
  font-size: 28rpx;
}

/* 扩展选项样式 */
.options-card {
  margin: 0 20rpx 20rpx;
  padding: 24rpx;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.option-group {
  background: #F8F6F3;
  border-radius: 16rpx;
  padding: 20rpx;
  border-left: 4rpx solid #7B5B44;
}

.dish-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #2E2A27;
  margin-bottom: 12rpx;
  display: block;
}

.option-items {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.option-label {
  font-size: 24rpx;
  color: #6A625B;
  font-weight: bold;
  min-width: 80rpx;
}

.option-value {
  font-size: 24rpx;
  color: #2E2A27;
  flex: 1;
}
</style>
