<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input class="search-input" placeholder="搜索菜品..." v-model="searchKeyword" @input="onSearch" />
    </view>
    
    <!-- 主内容区：左侧分类 + 右侧菜品 -->
    <view class="main-content">
      <!-- 左侧分类栏 -->
      <view class="category-sidebar">
        <view 
          class="category-item"
          :class="{ 'active': currentCategory === '' }"
          @click="selectCategory('')"
        >
          <text>全部</text>
        </view>
        <view 
          v-for="cat in categories" 
          :key="cat"
          class="category-item"
          :class="{ 'active': currentCategory === cat }"
          @click="selectCategory(cat)"
        >
          <text>{{ cat }}</text>
        </view>
      </view>
      
      <!-- 右侧菜品列表 -->
      <view class="dish-container">
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
              </view>
              <view class="order-count-bottom text-muted">已点{{ dish.orderCount }}次</view>
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
    </view>
    
    <!-- 管理员添加菜品按钮 -->
    <view v-if="isAdmin" class="fab-button primary-bg" @click="goToAddDish">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script>
import { dishApi } from '@/utils/api.js'
import cartManager from '@/utils/cart.js'
import userManager from '@/utils/user.js'

export default {
  data() {
    return {
      dishes: [],
      categories: ['肉类', '蔬菜', '主食', '凉菜', '汤'],
      currentCategory: '',
      searchKeyword: '',
      filteredDishes: [],
      isAdmin: false
    }
  },
  
  onLoad() {
    this.checkAdminStatus()
    this.loadDishes()
    
    // 监听用户信息更新事件
    uni.$on('userInfoUpdated', this.onUserInfoUpdated)
  },
  
  onUnload() {
    // 移除事件监听
    uni.$off('userInfoUpdated', this.onUserInfoUpdated)
  },
  
  onShow() {
    // 每次显示页面时刷新数据和管理员状态
    this.checkAdminStatus()
    this.loadDishes()
  },
  
  methods: {
    // 用户信息更新后的回调
    onUserInfoUpdated(userInfo) {
      console.log('接收到用户信息更新事件:', userInfo)
      this.checkAdminStatus()
    },
    
    // 检查管理员状态
    checkAdminStatus() {
      this.isAdmin = userManager.isAdmin()
      console.log('管理员状态:', this.isAdmin)
      console.log('用户信息:', userManager.getUserInfo())
    },
    
    // 选择分类
    selectCategory(category) {
      this.currentCategory = category
      this.filterDishes()
    },
    
    // 筛选菜品
    filterDishes() {
      let result = this.dishes
      
      // 按分类筛选
      if (this.currentCategory) {
        result = result.filter(dish => dish.category === this.currentCategory)
      }
      
      // 按关键词搜索
      if (this.searchKeyword) {
        result = result.filter(dish => 
          dish.name.includes(this.searchKeyword) || 
          (dish.description && dish.description.includes(this.searchKeyword))
        )
      }
      
      this.filteredDishes = result
    },
    
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
      this.filterDishes()
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
    },
    
    // 跳转到添加菜品页面
    goToAddDish() {
      uni.navigateTo({
        url: '/pages/dish/add'
      })
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.search-bar {
  padding: 20rpx;
  background-color: #ffffff;
}

.search-input {
  background-color: #f8f8f8;
  border-radius: 50rpx;
  padding: 20rpx 30rpx;
  font-size: 28rpx;
}

/* 主内容区：左右布局 */
.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 左侧分类栏 */
.category-sidebar {
  width: 160rpx;
  background-color: #f8f8f8;
  overflow-y: auto;
}

.category-item {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #666666;
  background-color: #f8f8f8;
  position: relative;
  transition: all 0.3s;
}

.category-item.active {
  background-color: #ffffff;
  color: #FF6B6B;
  font-weight: bold;
}

.category-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6rpx;
  height: 40rpx;
  background-color: #FF6B6B;
  border-radius: 0 4rpx 4rpx 0;
}

/* 右侧菜品容器 */
.dish-container {
  flex: 1;
  background-color: #ffffff;
  overflow-y: auto;
}

.dish-list {
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  position: relative;
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 20rpx;
}

.dish-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  overflow: hidden;
  flex-shrink: 0;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dish-image image {
  width: 100%;
  height: 100%;
}

.placeholder-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #cccccc;
  font-size: 24rpx;
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

.order-count-bottom {
  font-size: 22rpx;
  color: #999999;
  margin-top: 4rpx;
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

.fab-button {
  position: fixed;
  right: 40rpx;
  bottom: 120rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba(255, 107, 107, 0.4);
  z-index: 1000;
}

.fab-icon {
  color: #ffffff;
  font-size: 60rpx;
  font-weight: bold;
}
</style>
