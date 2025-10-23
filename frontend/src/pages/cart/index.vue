<template>
  <view class="container">
    <!-- 购物车列表 -->
    <view v-if="cartItems.length > 0" class="cart-list">
      <view class="cart-item card" v-for="item in cartItems" :key="item.id">
        <view class="item-image">
          <image 
            v-if="item.imageUrl" 
            :src="item.imageUrl" 
            mode="aspectFill"
          />
          <view v-else class="placeholder-image">
            <text>无图</text>
          </view>
        </view>
        
        <view class="item-info">
          <text class="item-name">{{ item.name }}</text>
          <text class="item-price price-small">¥{{ item.price }}</text>
        </view>
        
        <view class="item-actions">
          <view class="quantity-control">
            <view class="control-btn" @click="decreaseQuantity(item)">-</view>
            <text class="quantity">{{ item.quantity }}</text>
            <view class="control-btn" @click="increaseQuantity(item)">+</view>
          </view>
          <view class="delete-btn" @click="removeItem(item)">
            <text>删除</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 空购物车 -->
    <view v-else class="empty-cart">
      <text class="empty-text">购物车空空如也~</text>
      <button class="btn btn-primary primary-bg" @click="goShopping">去点餐</button>
    </view>
    
    <!-- 备注 -->
    <view v-if="cartItems.length > 0" class="remark-card card">
      <text class="label">备注信息</text>
      <textarea 
        class="remark-input" 
        v-model="remark" 
        placeholder="有什么想对老公说的吗？❤️"
        maxlength="200"
      />
    </view>
    
    <!-- 底部结算 -->
    <view v-if="cartItems.length > 0" class="bottom-bar">
      <view class="total-info">
        <text class="total-label">合计:</text>
        <text class="total-price price">¥{{ totalAmount }}</text>
      </view>
      <button class="submit-btn" @click="submitOrder">
        <text class="submit-text">提交订单</text>
      </button>
    </view>
  </view>
</template>

<script>
import cartManager from '@/utils/cart.js'
import { orderApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'

export default {
  data() {
    return {
      cartItems: [],
      remark: ''
    }
  },
  
  computed: {
    totalAmount() {
      return cartManager.getTotalAmount().toFixed(2)
    }
  },
  
  onShow() {
    this.loadCart()
  },
  
  methods: {
    // 加载购物车
    loadCart() {
      this.cartItems = cartManager.getCart()
    },
    
    // 减少数量
    decreaseQuantity(item) {
      if (item.quantity > 1) {
        cartManager.updateQuantity(item.id, item.quantity - 1)
        this.loadCart()
      }
    },
    
    // 增加数量
    increaseQuantity(item) {
      cartManager.updateQuantity(item.id, item.quantity + 1)
      this.loadCart()
    },
    
    // 移除商品
    removeItem(item) {
      uni.showModal({
        title: '提示',
        content: '确定要删除这个菜品吗？',
        success: (res) => {
          if (res.confirm) {
            cartManager.removeFromCart(item.id)
            this.loadCart()
            uni.showToast({
              title: '已删除',
              icon: 'success'
            })
          }
        }
      })
    },
    
    // 去购物
    goShopping() {
      uni.switchTab({
        url: '/pages/index/index'
      })
    },
    
    // 提交订单
    async submitOrder() {
      if (this.cartItems.length === 0) {
        uni.showToast({
          title: '购物车为空',
          icon: 'none'
        })
        return
      }
      
      try {
        uni.showLoading({ title: '提交中...' })
        
        // 构建订单数据
        const orderData = {
          remark: this.remark,
          items: this.cartItems.map(item => ({
            dishId: item.id,
            quantity: item.quantity
          }))
        }
        
        // 创建订单
        const orderId = await orderApi.create(orderData)
        
        // 清空购物车
        cartManager.clearCart()
        this.remark = ''
        
        uni.hideLoading()
        
        // 根据用户角色显示不同提示
        const isAdmin = userManager.isAdmin()
        const title = isAdmin ? '订单创建成功' : '订单提交成功'
        const content = isAdmin ? '下单成功' : '等待老公确认中...❤️'
        
        // 提示成功
        uni.showModal({
          title: title,
          content: content,
          showCancel: false,
          success: () => {
            // 跳转到订单列表
            uni.switchTab({
              url: '/pages/order/list'
            })
          }
        })
      } catch (error) {
        uni.hideLoading()
        console.error('提交订单失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  padding: 20rpx;
  padding-bottom: 180rpx;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(123, 91, 68, 0.06);
}

.item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
  background-color: #EFE7DD;
}

.item-image image {
  width: 100%;
  height: 100%;
}

.placeholder-image {
  width: 100%;
  height: 100%;
  font-size: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.item-name {
  font-size: 28rpx;
  color: #2E2A27;
  font-weight: bold;
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  align-items: flex-end;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.control-btn {
  width: 44rpx;
  height: 44rpx;
  border: 2rpx solid #E2D8CC;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #6A625B;
}

.quantity {
  font-size: 28rpx;
  font-weight: bold;
  min-width: 40rpx;
  text-align: center;
}

.delete-btn {
  font-size: 24rpx;
  color: #A39A92;
  padding: 8rpx 16rpx;
}

.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
  gap: 40rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #A39A92;
}

.remark-card {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.remark-input {
  width: 100%;
  min-height: 150rpx;
  background-color: #F6F3EF;
  border-radius: 16rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 24rpx 32rpx;
  box-shadow: 0 -8rpx 24rpx rgba(123, 91, 68, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1rpx solid rgba(123, 91, 68, 0.1);
}

.total-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
}

.total-label {
  font-size: 28rpx;
  color: #6A625B;
  font-weight: 500;
}

.total-price {
  font-size: 32rpx;
  font-weight: bold;
  color: #7B5B44;
}

.submit-btn {
  background: linear-gradient(135deg, #7B5B44 0%, #9F7A5A 100%);
  border: none;
  border-radius: 50rpx;
  padding: 10rpx 20rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  box-shadow: 0 6rpx 20rpx rgba(123, 91, 68, 0.25);
  transition: all 0.2s ease;
  min-width: 200rpx;
  justify-content: center;
}

.submit-btn:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 16rpx rgba(123, 91, 68, 0.35);
}

.submit-text {
  color: #ffffff;
  font-size: 30rpx;
  font-weight: bold;
}

.submit-icon {
  color: #ffffff;
  font-size: 28rpx;
  font-weight: bold;
}
</style>
