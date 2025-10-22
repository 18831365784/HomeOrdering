<template>
  <view class="container">
    <view v-if="dish" class="dish-detail">
      <!-- 菜品图片 -->
      <view class="dish-banner">
        <image 
          v-if="dish.imageUrl" 
          :src="dish.imageUrl" 
          mode="aspectFill"
        />
        <view v-else class="placeholder-image">
          <text>暂无图片</text>
        </view>
      </view>
      
      <!-- 菜品信息卡片 -->
      <view class="dish-info-card card">
        <view class="dish-header">
          <text class="dish-name">{{ dish.name }}</text>
          <text class="order-count badge badge-info">已点{{ dish.orderCount }}次</text>
        </view>
        
        <view class="dish-price">
          <text class="price">¥{{ dish.price }}</text>
        </view>
        
        <view class="dish-description">
          <text class="label">菜品简介</text>
          <text class="content">{{ dish.description || '暂无简介' }}</text>
        </view>
      </view>
      
      <!-- 数量选择 -->
      <view class="quantity-card card">
        <text class="label">数量</text>
        <view class="quantity-selector">
          <view class="quantity-btn" @click="decreaseQuantity">-</view>
          <text class="quantity-value">{{ quantity }}</text>
          <view class="quantity-btn" @click="increaseQuantity">+</view>
        </view>
      </view>
      
      <!-- 底部按钮 -->
      <view class="bottom-actions">
        <view class="total-price">
          <text class="label">小计:</text>
          <text class="price">¥{{ totalPrice }}</text>
        </view>
        <view class="action-buttons">
          <button class="btn btn-secondary" @click="addToCart">加入购物车</button>
          <button class="btn btn-primary primary-bg" @click="buyNow">立即下单</button>
        </view>
      </view>
    </view>
    
    <!-- 加载中 -->
    <view v-else class="loading">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script>
import { dishApi } from '@/utils/api.js'
import cartManager from '@/utils/cart.js'

export default {
  data() {
    return {
      dishId: null,
      dish: null,
      quantity: 1
    }
  },
  
  computed: {
    totalPrice() {
      if (!this.dish) return 0
      return (this.dish.price * this.quantity).toFixed(2)
    }
  },
  
  onLoad(options) {
    this.dishId = options.id
    this.loadDishDetail()
  },
  
  methods: {
    // 加载菜品详情
    async loadDishDetail() {
      try {
        uni.showLoading({ title: '加载中...' })
        const dish = await dishApi.getDetail(this.dishId)
        this.dish = dish
      } catch (error) {
        console.error('加载菜品详情失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },
    
    // 减少数量
    decreaseQuantity() {
      if (this.quantity > 1) {
        this.quantity--
      }
    },
    
    // 增加数量
    increaseQuantity() {
      this.quantity++
    },
    
    // 加入购物车
    addToCart() {
      cartManager.addToCart(this.dish, this.quantity)
      uni.showToast({
        title: '已添加到购物车',
        icon: 'success'
      })
      
      // 返回上一页
      setTimeout(() => {
        uni.navigateBack()
      }, 1000)
    },
    
    // 立即下单
    buyNow() {
      // 清空购物车，只购买当前菜品
      cartManager.clearCart()
      cartManager.addToCart(this.dish, this.quantity)
      
      // 跳转到购物车页面
      uni.switchTab({
        url: '/pages/cart/index'
      })
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  padding-bottom: 180rpx;
}

.dish-banner {
  width: 100%;
  height: 500rpx;
  background-color: #f0f0f0;
}

.dish-banner image {
  width: 100%;
  height: 100%;
}

.placeholder-image {
  width: 100%;
  height: 100%;
}

.dish-info-card {
  margin: 20rpx;
}

.dish-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.dish-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #333333;
}

.dish-price {
  margin-bottom: 30rpx;
}

.dish-description {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.label {
  font-size: 28rpx;
  color: #666666;
  font-weight: bold;
}

.content {
  font-size: 28rpx;
  color: #999999;
  line-height: 1.6;
}

.quantity-card {
  margin: 0 20rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.quantity-btn {
  width: 60rpx;
  height: 60rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #666666;
}

.quantity-value {
  font-size: 32rpx;
  font-weight: bold;
  min-width: 60rpx;
  text-align: center;
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 20rpx;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.total-price {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
  padding: 0 10rpx;
}

.action-buttons {
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
  padding: 100rpx 0;
  color: #999999;
  font-size: 28rpx;
}
</style>
