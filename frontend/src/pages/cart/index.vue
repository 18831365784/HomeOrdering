<template>
  <view class="container">
    <!-- 购物车列表 -->
    <view v-if="cartItems.length > 0" class="cart-list">
      <view class="cart-item card" v-for="item in cartItems" :key="item.id">
        <view class="item-image">
          <SafeImage 
            v-if="item.imageUrl" 
            :src="item.imageUrl" 
            imgClass="item-image-img"
            mode="aspectFill"
          />
          <view v-else class="placeholder-image">
            <text>无图</text>
          </view>
        </view>
        
        <view class="item-info">
          <text class="item-name">{{ item.name }}</text>
          <!-- 显示扩展选项 -->
          <view v-if="item.selectedOptions && Object.keys(item.selectedOptions).length > 0" class="selected-options">
            <text v-for="(value, key) in item.selectedOptions" :key="key" class="option-tag">
              {{ formatOptionDisplay(key, value) }}
            </text>
          </view>
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
      <button class="btn btn-coral" @click="goShopping">去点餐</button>
    </view>
    
    <!-- 备注 -->
    <view v-if="cartItems.length > 0" class="remark-card card">
      <text class="label">备注信息</text>
      <textarea
        class="remark-input"
        v-model="remark"
        placeholder="口味、忌口等备注"
        maxlength="200"
      />
    </view>

    <!-- 制作人选择 -->
    <view v-if="cartItems.length > 0" class="maker-section card">
      <view class="maker-header">
        <text class="maker-title">选择制作人</text>
      </view>
      <view class="maker-list">
        <view
          v-for="member in familyMembers"
          :key="member.uuid"
          class="maker-item"
          :class="{ active: selectedMaker?.uuid === member.uuid }"
          @click="selectMaker(member)"
        >
          <image class="maker-avatar" :src="member.avatarUrl || '/static/icons/home.png'" mode="aspectFill" />
          <view class="maker-info">
            <text class="maker-name">{{ member.nickname || '用户' }}</text>
            <text v-if="member.isAdmin" class="maker-tag">管理员</text>
          </view>
          <view v-if="selectedMaker?.uuid === member.uuid" class="maker-check">✓</view>
        </view>
      </view>
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
import { familyApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'
import SafeImage from '@/components/SafeImage.vue'

export default {
  components: { SafeImage },
  data() {
    return {
      cartItems: [],
      remark: '',
      familyMembers: [],
      selectedMaker: null
    }
  },

  computed: {
    totalAmount() {
      const amount = cartManager.getTotalAmount()
      console.log('计算总金额:', amount, '购物车项目:', this.cartItems.length)
      return amount.toFixed(2)
    }
  },

  onShow() {
    this.loadCart()
    this.loadFamilyMembers()
  },

  methods: {
    // 加载购物车
    loadCart() {
      this.cartItems = cartManager.getCart()
      console.log('购物车加载完成:', this.cartItems)
      console.log('总金额:', cartManager.getTotalAmount())
    },

    // 加载家庭成员
    async loadFamilyMembers() {
      try {
        const uuid = userManager.getUuid()
        if (!uuid) return

        const info = await familyApi.getFamilyInfo()
        if (info && info.members) {
          this.familyMembers = info.members
          // 默认选中自己
          const currentUser = userManager.getUserInfo()
          const myInfo = info.members.find(m => m.uuid === uuid)
          if (myInfo) {
            this.selectedMaker = myInfo
          }
        }
      } catch (e) {
        console.error('加载家庭成员失败', e)
      }
    },

    // 选择制作人
    selectMaker(member) {
      this.selectedMaker = member
    },
    
    // 减少数量
    decreaseQuantity(item) {
      if (item.quantity > 1) {
        cartManager.updateQuantityByKey(item.key, item.quantity - 1)
        this.loadCart()
      }
    },
    
    // 增加数量
    increaseQuantity(item) {
      cartManager.updateQuantityByKey(item.key, item.quantity + 1)
      this.loadCart()
    },
    
    // 移除商品
    removeItem(item) {
      uni.showModal({
        title: '提示',
        content: '确定要删除这个菜品吗？',
        success: (res) => {
          if (res.confirm) {
            cartManager.removeFromCart(item.key)
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
      if (!this.selectedMaker || !this.selectedMaker.uuid) {
        uni.showToast({
          title: '请选择制作人',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({ title: '提交中...' })

        // 构建订单数据（身份与家庭以服务端登录态为准）
        const orderData = {
          remark: this.buildOrderRemark(),
          items: this.cartItems.map(item => ({
            dishId: item.id,
            quantity: item.quantity,
            unitPrice: item.price
          })),
          makerUuid: this.selectedMaker.uuid
        }

        console.log('提交订单数据:', orderData)
        console.log('购物车项目详情:', this.cartItems)
        
        // 创建订单
        const orderId = await orderApi.create(orderData)
        
        // 清空购物车
        cartManager.clearCart()
        this.remark = ''
        
        uni.hideLoading()
        
        uni.showModal({
          title: '订单提交成功',
          content: '已通知制作人，可在订单页查看进度',
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
    },
    
    // 格式化选项显示
    formatOptionDisplay(key, value) {
      if (Array.isArray(value)) {
        return `${key}: ${value.join(', ')}`
      } else if (typeof value === 'object' && value !== null) {
        return `${key}: ${JSON.stringify(value)}`
      } else {
        return `${key}: ${value}`
      }
    },
    
    // 构建订单备注，包含扩展选项信息
    buildOrderRemark() {
      let remark = this.remark || ''
      
      // 收集所有有扩展选项的菜品信息
      const itemsWithOptions = this.cartItems.filter(item => 
        item.selectedOptions && Object.keys(item.selectedOptions).length > 0
      )
      
      if (itemsWithOptions.length > 0) {
        remark += '\n\n【菜品选项】:'
        itemsWithOptions.forEach(item => {
          remark += `\n${item.name}(${item.quantity}份):`
          Object.entries(item.selectedOptions).forEach(([key, value]) => {
            remark += `\n  ${key}: ${this.formatOptionValue(value)}`
          })
        })
      }
      
      return remark.trim()
    },
    
    // 格式化选项值
    formatOptionValue(value) {
      if (Array.isArray(value)) {
        return value.join(', ')
      } else if (typeof value === 'object' && value !== null) {
        return JSON.stringify(value)
      } else {
        return String(value)
      }
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  padding: 20rpx;
  padding-top: calc(20rpx + constant(safe-area-inset-top));
  padding-top: calc(20rpx + env(safe-area-inset-top));
  padding-bottom: 180rpx;
  padding-bottom: calc(180rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(180rpx + env(safe-area-inset-bottom));
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
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 107, 0.08);
}

.item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0F0F0 100%);
}

:deep(.item-image-img) {
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
  color: #212121;
  font-weight: bold;
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  align-items: flex-end;
}

.quantity-control {
  display: inline-flex;
  align-items: center;
  background: #F5F5F5;
  border-radius: 50rpx;
  padding: 4rpx;
}

.control-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #4ECDC4;
  background: transparent;
  border: none;
  padding: 0;
}

.control-btn:active {
  background: rgba(78, 205, 196, 0.2);
}

.quantity {
  font-size: 28rpx;
  font-weight: bold;
  min-width: 56rpx;
  text-align: center;
  color: #212121;
}

.delete-btn {
  font-size: 24rpx;
  color: #9E9E9E;
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
  background-color: #F5F5F5;
  border: 2rpx solid transparent;
  border-radius: 16rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.remark-input:focus {
  border-color: #FF6B6B;
  background: #FFFFFF;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.total-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
}

.total-label {
  font-size: 28rpx;
  color: #616161;
  font-weight: 500;
}

.total-price {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF6B6B;
}

.submit-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  border: none;
  border-radius: 40rpx;
  padding: 20rpx 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.submit-text {
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 600;
}

/* 扩展选项样式 */
.selected-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin: 8rpx 0;
}

.option-tag {
  background: rgba(255, 107, 107, 0.1);
  color: #FF6B6B;
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
}

/* 制作人选择 */
.maker-section {
  margin: 0 20rpx 180rpx;
}

.maker-header {
  margin-bottom: 20rpx;
}

.maker-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #212121;
}

.maker-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.maker-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background: #F8F8F8;
  border-radius: 16rpx;
  border: 3rpx solid transparent;
  transition: all 0.2s ease;
}

.maker-item.active {
  border-color: #4ECDC4;
  background: rgba(78, 205, 196, 0.1);
}

.maker-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.maker-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.maker-name {
  font-size: 30rpx;
  color: #212121;
  font-weight: 500;
}

.maker-tag {
  font-size: 20rpx;
  color: #4ECDC4;
  background: rgba(78, 205, 196, 0.15);
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.maker-check {
  width: 48rpx;
  height: 48rpx;
  background: linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-size: 28rpx;
  font-weight: bold;
}
</style>
