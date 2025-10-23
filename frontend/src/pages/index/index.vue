<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-shell">
        <text class="search-icon">🔎</text>
        <input class="search-input" placeholder="搜索美食、饮品..." v-model="searchKeyword" @input="onSearch" />
      </view>
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
          <view class="cat-stack">
            <text class="cat-icon cat-icon-text">{{ categoryIcons['全部'] }}</text>
            <text class="cat-text">全部</text>
          </view>
        </view>
        <view 
          v-for="cat in categories" 
          :key="cat"
          class="category-item"
          :class="{ 'active': currentCategory === cat }"
          @click="selectCategory(cat)"
        >
          <view class="cat-stack">
            <template v-if="categoryIcons[cat] && (categoryIcons[cat].indexOf('http://') === 0 || categoryIcons[cat].indexOf('https://') === 0 || categoryIcons[cat].indexOf('/uploads/') === 0)">
              <image class="cat-icon-img" :src="categoryIcons[cat]" mode="aspectFit" />
            </template>
            <template v-else>
              <text class="cat-icon cat-icon-text">{{ categoryIcons[cat] || '●' }}</text>
            </template>
            <text class="cat-text">{{ cat }}</text>
          </view>
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
              <view class="divider" />
              <view class="dish-meta">
                <text class="price">¥{{ dish.price }}</text>
                <text class="order-count">销量 {{ dish.orderCount || 0 }}</text>
              </view>
            </view>
            
            <!-- 添加/选择按钮：有扩展项则展示“选择”并跳详情 -->
            <view class="dish-action">
              <block v-if="needChoose(dish)">
                <button class="choose-btn" @click.stop="goToDetail(dish.id)">选择</button>
              </block>
              <block v-else>
                <view class="add-btn" @click.stop="addToCart(dish)">
                  <text class="add-plus">＋</text>
                </view>
              </block>
            </view>
          </view>
          
          <!-- 空状态 -->
          <view v-if="filteredDishes.length === 0" class="empty-state">
            <text>暂无菜品</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 管理员菜品管理入口（右下角主悬浮） -->
    <view v-if="isAdmin" class="fab-button primary-bg" @click="goDishManage">
      <text class="fab-icon">🍽️</text>
    </view>

    <!-- 管理员分类管理入口（右下角次悬浮） -->
    <view v-if="isAdmin" class="fab-button fab-secondary accent-bg" @click="goCategoryManage">
      <text class="fab-icon">🏷️</text>
    </view>

    <!-- 已移除内嵌新增分类弹窗，改为单独分类管理页 -->
  </view>
</template>

<script>
import { dishApi, categoryApi } from '@/utils/api.js'
import cartManager from '@/utils/cart.js'
import userManager from '@/utils/user.js'

export default {
  data() {
    return {
      dishes: [],
      categories: [],
      currentCategory: '',
      searchKeyword: '',
      filteredDishes: [],
      isAdmin: false,
      categoryIcons: { '全部': '🏷️' }
    }
  },
  
  onLoad() {
    this.checkAdminStatus()
    this.loadCategories()
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
    // 是否需要进入详情选择
    needChoose(dish) {
      if (!dish) return false
      const ext = dish.extensions
      if (!ext) return false
      let obj = null
      if (typeof ext === 'string') {
        try { obj = JSON.parse(ext) } catch (e) { return false }
      } else if (typeof ext === 'object') {
        obj = ext
      }
      if (!obj) return false
      const options = Array.isArray(obj.options) ? obj.options : []
      return options.length > 0
    },
    
    // 判断是否为图片URL（支持 http/https 以及 /uploads 开头的后端静态资源）
    isImageUrl(v) {
      if (!v || typeof v !== 'string') return false
      return v.startsWith('http://') || v.startsWith('https://') || v.startsWith('/uploads/')
    },
    // 加载分类列表
    async loadCategories() {
      try {
        const list = await categoryApi.getList(1)
        this.categories = list.map(c => c.name)
        // 合并后端图标到映射（如果有）
        list.forEach(c => {
          if (c.iconUrl) this.categoryIcons[c.name] = c.iconUrl
        })
        
      } catch (e) {
        console.error('加载分类失败', e)
      }
    },
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
    // 跳转菜品管理
    goDishManage() {
      uni.navigateTo({ url: '/pages/dish/manage' })
    },
    
    // 跳转分类管理
    goCategoryManage() {
      uni.navigateTo({ url: '/pages/category/manage' })
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
  background-color: #F6F3EF;
}

.search-shell {
  display: flex;
  align-items: center;
  background-color: #ffffff;
  border-radius: 999rpx;
  padding: 16rpx 24rpx;
  box-shadow: 0 6rpx 18rpx rgba(123, 91, 68, 0.06);
}

.search-icon {
  margin-right: 12rpx;
  color: #A39A92;
  font-size: 28rpx;
}

.search-input {
  flex: 1;
  background-color: transparent;
  border-radius: 999rpx;
  padding: 10rpx 6rpx;
  font-size: 28rpx;
}

/* 主内容区：左右布局 */
.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
  padding-left: 160rpx; /* 为固定侧栏预留空间 */
}

/* 左侧分类栏 */
.category-sidebar {
  width: 160rpx;
  background-color: #F0E9E1;
  overflow-y: auto;
  overflow-x: hidden;
  position: fixed;
  left: 0;
  top: 128rpx; /* 稍微向下调整，与右侧菜品栏对齐 */
  height: calc(100vh - 120rpx);
  z-index: 900;
  -webkit-overflow-scrolling: touch; /* iOS 平滑滚动 */
  scrollbar-width: none; /* Firefox 隐藏滚动条 */
  -ms-overflow-style: none; /* IE/Edge 隐藏滚动条 */
}

/* Webkit 浏览器隐藏滚动条 */
.category-sidebar::-webkit-scrollbar {
  display: none;
}


.category-item {
  height: 140rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #666666;
  background-color: transparent;
  position: relative;
  transition: all 0.3s;
  padding: 0 12rpx;
  flex-shrink: 0; /* 防止分类项被压缩 */
}

.category-item.active {
  background-color: #ffffff;
  color: #7B5B44;
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
  background-color: #7B5B44;
  border-radius: 0 4rpx 4rpx 0;
}

.cat-stack { display:flex; flex-direction: column; align-items: center; justify-content:center; gap: 8rpx; width: 100%; }
.cat-icon { width: 48rpx; text-align: center; }
.cat-icon-text { font-size: 40rpx; }
.cat-icon-img { width: 56rpx; height: 56rpx; }
.cat-text { font-size: 28rpx; color: #2E2A27; }

/* 右侧菜品容器 */
.dish-container {
  flex: 1;
  background-color: #ffffff;
  overflow-y: auto;
  height: calc(100vh - 120rpx);
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
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(123, 91, 68, 0.06);
}

.dish-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
  background-color: #EFE7DD;
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
  color: #2E2A27;
}

.dish-desc {
  font-size: 24rpx;
  color: #6A625B;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-meta {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.price {
  font-size: 32rpx;
  color: #7B5B44;
  font-weight: bold;
}

.order-count {
  font-size: 24rpx;
  color: #A39A92;
}

.dish-action {
  position: absolute;
  right: 20rpx;
  bottom: 20rpx;
}

.add-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 36rpx;
  font-weight: bold;
  background: linear-gradient(135deg, #7B5B44 0%, #9F7A5A 100%);
  box-shadow: 0 4rpx 12rpx rgba(123, 91, 68, 0.25);
  border: 2rpx solid rgba(255, 255, 255, 0.2);
  transition: all 0.2s ease;
}

.add-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(123, 91, 68, 0.35);
}

.add-plus { 
  line-height: 1; 
  margin-top: -2rpx; /* 微调加号位置 */
}

.choose-btn {
  background: linear-gradient(135deg, #7B5B44 0%, #9F7A5A 100%);
  color: #fff;
  border: none;
  border-radius: 999rpx;
  padding: 12rpx 24rpx;
  font-size: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(123, 91, 68, 0.25);
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
  box-shadow: 0 10rpx 28rpx rgba(123, 91, 68, 0.30);
  z-index: 1000;
}

.fab-icon {
  color: #ffffff;
  font-size: 60rpx;
  font-weight: bold;
}

.fab-secondary { bottom: 240rpx; }

.modal-mask {
  position: fixed; left: 0; right: 0; top: 0; bottom: 0;
  background: rgba(0,0,0,0.35);
  display: flex; align-items: center; justify-content: center;
  z-index: 2000;
}
.modal-card { width: 640rpx; }
.modal-title { font-size: 32rpx; font-weight: bold; color: #2E2A27; margin-bottom: 20rpx; }
.modal-body { display: flex; flex-direction: column; gap: 20rpx; }
.modal-input { background:#F6F3EF; border-radius: 16rpx; padding: 20rpx; font-size: 28rpx; }
.upload-row { display:flex; align-items:center; gap: 16rpx; }
.icon-preview { width: 64rpx; height: 64rpx; border-radius: 12rpx; background:#EFE7DD; }
.modal-actions { display:flex; gap: 20rpx; margin-top: 12rpx; }
</style>
