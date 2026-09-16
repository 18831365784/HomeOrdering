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
          <view class="dish-item" v-for="(detail, index) in order.details" :key="detail.dishId + '_' + index">
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
              <!-- 显示该菜品的扩展选项 -->
              <view v-if="getDishOptionsByDetail(detail, index)" class="dish-options">
                <text 
                  v-for="option in getDishOptionsByDetail(detail, index)" 
                  :key="option.key" 
                  class="option-text"
                >
                  {{ option.key }}: {{ formatOptionValue(option.value) }}
                </text>
              </view>
              <text class="dish-meta">¥{{ detail.dishPrice }} x {{ detail.quantity }}</text>
            </view>
            <text class="dish-subtotal price-small">¥{{ detail.subtotal }}</text>
          </view>
        </view>
      </view>
      
      <!-- 备注信息 -->
      <view v-if="order.remark && formatRemark(order.remark).trim()" class="remark-card card">
        <text class="card-title">备注信息</text>
        <text class="remark-content">{{ formatRemark(order.remark) }}</text>
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
      <view class="action-buttons" v-if="showActionButton">
        <!-- 制作人 - 待接单状态 -->
        <button
          v-if="isMaker && order.status === 0"
          class="action-btn action-btn-primary"
          @click="handleAccept"
        >
          接单
        </button>

        <!-- 下单人 - 待接单状态 -->
        <button
          v-if="isCustomer && order.status === 0"
          class="action-btn action-btn-outline"
          @click="handleCancel"
        >
          取消订单
        </button>

        <!-- 制作人 - 制作中状态 -->
        <button
          v-if="isMaker && order.status === 1"
          class="action-btn action-btn-primary"
          @click="handleFinish"
        >
          完成订单
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
import SafeImage from '@/components/SafeImage.vue'

export default {
  components: { SafeImage },
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
      return this.isMaker || this.isCustomer
    }
  },

  onLoad(options) {
    this.orderId = options.id
    this.currentUuid = userManager.getUuid()
    this.loadOrderDetail()
  },

  methods: {
    // 检查用户角色
    checkUserRole() {
      if (!this.order || !this.currentUuid) return

      this.isMaker = this.order.makerUuid === this.currentUuid
      this.isCustomer = this.order.customerUuid === this.currentUuid
      console.log('订单详情页角色检查:', {
        isMaker: this.isMaker,
        isCustomer: this.isCustomer,
        orderMaker: this.order.makerUuid,
        orderCustomer: this.order.customerUuid,
        currentUuid: this.currentUuid
      })
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
        // 检查用户角色
        this.checkUserRole()
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

    // 接单（制作人操作）
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
              console.error('接单失败:', error)
            }
          }
        }
      })
    },

    // 取消订单（下单人操作）
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
              console.error('取消订单失败:', error)
            }
          }
        }
      })
    },

    // 完成订单（制作人操作）
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
              console.error('完成订单失败:', error)
            }
          }
        }
      })
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        0: '待接单',
        1: '制作中',
        2: '已完成',
        '-1': '已取消'
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
        // 修复：支持中文菜品名
        if (skipOptions && line.match(/^\s*.+\(\d+份\):$/)) {
          continue
        }
        // 修复：支持中文选项名
        if (skipOptions && line.match(/^\s*.+:\s*.+$/)) {
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
    
    // 根据菜品明细获取对应的扩展选项
    getDishOptionsByDetail(detail, detailIndex) {
      console.log('菜品明细匹配:', {
        dishName: detail.dishName,
        quantity: detail.quantity,
        detailIndex: detailIndex,
        totalDetails: this.order.details.length
      })
      
      // 查找所有匹配的菜品选项（根据名称和数量）
      const matchedOptions = this.extractedOptions.filter(opt => 
        opt.dishName === detail.dishName && opt.quantity === detail.quantity
      )
      
      console.log('匹配的选项:', matchedOptions)
      
      // 如果有多个匹配项，根据索引选择对应的选项
      if (matchedOptions.length > 1) {
        // 使用索引来选择对应的选项，如果索引超出范围则使用最后一个
        const optionIndex = Math.min(detailIndex, matchedOptions.length - 1)
        console.log('选择选项索引:', optionIndex, '对应选项:', matchedOptions[optionIndex])
        return matchedOptions[optionIndex].items
      } else if (matchedOptions.length === 1) {
        console.log('唯一选项:', matchedOptions[0])
        return matchedOptions[0].items
      }
      
      console.log('未找到匹配选项')
      return null
    },
    
    // 获取特定菜品的扩展选项（保留原方法以兼容）
    getDishOptions(dishName) {
      // 查找所有匹配的菜品选项
      const matchedOptions = this.extractedOptions.filter(opt => opt.dishName === dishName)
      
      if (matchedOptions.length === 0) {
        return null
      }
      
      // 如果有多个相同名称的菜品，返回第一个的选项
      // 在实际应用中，可能需要更复杂的匹配逻辑
      return matchedOptions[0].items
    },
    
    // 格式化扩展项的值
    formatOptionValue(value) {
      if (value === true || value === 'true') {
        return '是'
      }
      if (value === false || value === 'false') {
        return '否'
      }
      if (Array.isArray(value)) {
        return value.join('、')
      }
      return value
    },
    
    // 从备注中提取扩展选项信息
    extractOptionsFromRemark(remark) {
      if (!remark) return []
      
      console.log('开始解析备注:', remark)
      
      const options = []
      const lines = remark.split('\n')
      let currentOption = null
      let inOptionsSection = false
      
      for (const line of lines) {
        console.log('处理行:', line)
        
        // 修复：匹配【菜品选项】: 格式（带冒号）
        if (line.includes('【菜品选项】')) {
          inOptionsSection = true
          console.log('进入选项解析区域')
          continue
        }
        
        if (inOptionsSection) {
          // 修复：匹配菜品名称(数量份): 格式，支持中文菜品名
          const dishMatch = line.match(/^(.+)\((\d+)份\):$/)
          if (dishMatch) {
            if (currentOption) {
              options.push(currentOption)
            }
            // 为每个菜品创建唯一标识，即使名称相同
            const dishName = dishMatch[1].trim()
            const quantity = parseInt(dishMatch[2])
            const uniqueId = `${dishName}_${quantity}_${options.length}`
            
            currentOption = {
              dishName: dishName,
              quantity: quantity,
              uniqueId: uniqueId,
              items: []
            }
            console.log('找到菜品:', currentOption.dishName, '数量:', currentOption.quantity, '唯一ID:', uniqueId)
            continue
          }
          
          // 修复：匹配选项: 值 格式，支持中文选项名
          const optionMatch = line.match(/^\s+(.+?):\s*(.+)$/)
          if (optionMatch && currentOption) {
            currentOption.items.push({
              key: optionMatch[1].trim(),
              value: optionMatch[2].trim()
            })
            console.log('找到选项:', optionMatch[1], '值:', optionMatch[2])
          }
        }
      }
      
      if (currentOption) {
        options.push(currentOption)
      }
      
      console.log('解析结果:', options)
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
  font-weight: bold;
}

.dish-options {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.option-text {
  font-size: 24rpx;
  color: #7B5B44;
  font-weight: 500;
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
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  text-align: center;
  border: none;
}

.action-btn-primary {
  background: linear-gradient(135deg, #66BB6A 0%, #81C784 100%);
  color: #FFFFFF;
}

.action-btn-outline {
  background: transparent;
  color: #9E9E9E;
  border: 2rpx solid #E0E0E0;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
  color: #999999;
  font-size: 28rpx;
}
</style>
