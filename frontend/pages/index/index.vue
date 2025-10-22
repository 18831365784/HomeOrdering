<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input class="search-input" placeholder="搜索菜品..." v-model="searchKeyword" @input="onSearch" />
    </view>
    
    <!-- 菜品列表 -->
    <view class="dish-list">
      <view 
        class="dish-item card" 
        v-for="dish in filteredDishes" 
        :key="dish.id"
        @click="goToDetail(dish.id)"
      >
        <!-- 菜品图片 -->
        <view class="dish-image">
          <image 
            v-if="dish.imageUrl" 
            :src="dish.imageUrl" 
            mode="aspectFill"
          />
          <view v-else class="placeholder-image">
            <text>暂无图片</text>
          </view>
        </view>
        
        <!-- 菜品信息 -->
        <view class="dish-info">
          <view class="dish-name">{{ dish.name }}</view>
          <view class="dish-desc">{{ dish.description || '暂无简介' }}</view>
          <view class="dish-meta">
            <text class="price">¥{{ dish.price }}</text>
            <text class="order-count text-muted">已点{{ dish.orderCount }}次</text>
          </view>
        </view>
        
        <!-- 添加按钮 -->
        <view class="dish-action" @click.stop="addToCart(dish)">
          <view class="add-btn primary-bg">
            <text>+</text>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="filteredDishes.length === 0" class="empty-state">
        <text>暂无菜品</text>
      </view>
    </view>
  </view>
</template>

<script>
import { dishApi } from '@/utils/api.js'
import cartManager from '@/utils/cart.js'

export default {
  data() {
    return {
      dishes: [],
      searchKeyword: '',
      filteredDishes: []
    }
  },
  
  onLoad() {
    this.loadDishes()
  },
  
  onShow() {
    // 每次显示页面时刷新数据
    this.loadDishes()
  },
  
  methods: {
    // 加载菜品列表
    async loadDishes() {
      try {
        uni.showLoading({ title: '加载中...' })
        const dishes = await dishApi.getList(1) // 只查询启用的菜品
        this.dishes = dishes
        this.filteredDishes = dishes
      } catch (error) {
        console.error('加载菜品失败:', error)
      } finally {
        uni.hideLoading()
      }
    },
    
    // 搜索菜品
    onSearch() {
      if (!this.searchKeyword) {
        this.filteredDishes = this.dishes
        return
      }
      
      this.filteredDishes = this.dishes.filter(dish => 
        dish.name.includes(this.searchKeyword) || 
        (dish.description && dish.description.includes(this.searchKeyword))
      )
    },
    
    // 跳转到详情页
    goToDetail(dishId) {
      uni.navigateTo({
        url: `/pages/dish/detail?id=${dishId}`
      })
    },
    
    // 添加到购物车
    addToCart(dish) {
      cartManager.addToCart(dish, 1)
      uni.showToast({
        title: '已添加到购物车',
        icon: 'success'
      })
    }
  }
}
</script>

<style scoped>
.container {
  padding: 20rpx;
  min-height: 100vh;
}

.search-bar {
  margin-bottom: 20rpx;
  padding: 0 20rpx;
}

.search-input {
  background-color: #ffffff;
  border-radius: 50rpx;
  padding: 20rpx 30rpx;
  font-size: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  position: relative;
}

.dish-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  overflow: hidden;
  flex-shrink: 0;
}

.dish-image image {
  width: 100%;
  height: 100%;
}

.placeholder-image {
  width: 100%;
  height: 100%;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.dish-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.dish-desc {
  font-size: 24rpx;
  color: #999999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-count {
  font-size: 22rpx;
}

.dish-action {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
}

.add-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 40rpx;
  font-weight: bold;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 107, 0.3);
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  color: #999999;
  font-size: 28rpx;
}
</style>
