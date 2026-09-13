<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-container">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          placeholder="搜索你喜欢的美食..."
          v-model="searchKeyword"
          @input="onSearch"
          border="none"
          :style="{ border: 'none' }"
        />
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
              <SafeImage imgClass="cat-icon-img" :src="categoryIcons[cat]" mode="aspectFill" />
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
            <template v-if="dish.imageUrl">
              <view class="dish-image">
                <SafeImage imgClass="dish-image-img" :src="dish.imageUrl" mode="aspectFill" />
              </view>
            </template>
            <template v-else>
              <view class="placeholder-image">
                <text>暂无图片</text>
              </view>
            </template>
            
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
  </view>
</template>

<script>
import { dishApi, categoryApi } from '@/utils/api.js'
import SafeImage from '@/components/SafeImage.vue'
import cartManager from '@/utils/cart.js'
import userManager from '@/utils/user.js'

export default {
  components: { SafeImage },
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
    // 监听分类更新事件
    uni.$on('categoryUpdated', this.onCategoryUpdated)
    // 监听菜品更新事件
    uni.$on('dishUpdated', this.onDishUpdated)
  },
  
  onUnload() {
    // 移除事件监听
    uni.$off('userInfoUpdated', this.onUserInfoUpdated)
    uni.$off('categoryUpdated', this.onCategoryUpdated)
    uni.$off('dishUpdated', this.onDishUpdated)
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
        const uuid = userManager.getUuid()
        const list = await categoryApi.getList(1, uuid)
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
    
    // 分类更新后的回调
    onCategoryUpdated() {
      console.log('接收到分类更新事件，刷新分类和菜品数据')
      this.loadCategories()
      this.loadDishes()
    },
    
    // 菜品更新后的回调
    onDishUpdated() {
      console.log('接收到菜品更新事件，刷新菜品数据')
      this.loadDishes()
    },
    
    // 检查管理员状态
    checkAdminStatus() {
      const userInfo = userManager.getUserInfo()
      this.isAdmin = userInfo && userInfo.role === 1
      console.log('管理员状态:', this.isAdmin)
      console.log('用户信息:', userInfo)
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
        const uuid = userManager.getUuid()
        const dishes = await dishApi.getList(1, uuid) // 只查询启用的菜品
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
  padding: 16rpx 24rpx;
  padding-top: calc(16rpx + constant(safe-area-inset-top));
  padding-top: calc(16rpx + env(safe-area-inset-top));
  background: #FFFFFF;
}

.search-container {
  display: flex;
  align-items: center;
  background: #F5F5F5;
  border-radius: 40rpx;
  padding: 0 28rpx;
  height: 80rpx;
  box-sizing: border-box;
}

.search-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  background: transparent;
  font-size: 28rpx;
  color: #212121;
  height: 80rpx;
  line-height: 80rpx;
}

.search-input::placeholder {
  color: #BDBDBD;
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
  background-color: #FFFFFF;
  overflow-y: auto;
  overflow-x: hidden;
  position: fixed;
  left: 0;
  top: 120rpx;
  height: calc(100vh - 120rpx);
  z-index: 90;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  -ms-overflow-style: none;
  border-right: 1rpx solid #F5F5F5;
}

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
  background-color: #FFF5F5;
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
  height: 48rpx;
  background-color: #FF6B6B;
  border-radius: 0 6rpx 6rpx 0;
}

.cat-stack { display:flex; flex-direction: column; align-items: center; justify-content:center; gap: 8rpx; width: 100%; }
.cat-icon { width: 48rpx; text-align: center; }
.cat-icon-text { font-size: 40rpx; }
:deep(.cat-icon-img) { width: 56rpx; height: 56rpx; }
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
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 107, 0.08);
  transition: all 0.2s ease;
}

.dish-item:active {
  box-shadow: 0 6rpx 20rpx rgba(255, 107, 107, 0.12);
}

.dish-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 20rpx;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0F0F0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.dish-image-img) {
  width: 190rpx;
  height: 190rpx;
}

.placeholder-image {
  width: 180rpx;
  height: 180rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #BDBDBD;
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
  color: #212121;
}

.dish-desc {
  font-size: 24rpx;
  color: #616161;
  line-height: 1.4;
  word-wrap: break-word;
  word-break: break-word;
  white-space: normal;
}

.dish-meta {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.price {
  font-size: 32rpx;
  color: #FF6B6B;
  font-weight: bold;
}

.order-count {
  font-size: 24rpx;
  color: #9E9E9E;
}

.dish-action {
  position: absolute;
  right: 20rpx;
  bottom: 20rpx;
}

.add-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 40rpx;
  font-weight: bold;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  box-shadow: 0 6rpx 20rpx rgba(255, 107, 107, 0.35);
  border: 3rpx solid rgba(255, 255, 255, 0.3);
  transition: all 0.2s ease;
}

.add-btn:active {
  transform: scale(0.9);
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 107, 0.45);
}

.add-plus {
  line-height: 1;
  margin-top: -2rpx;
}

.choose-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  color: #fff;
  border: none;
  border-radius: 50rpx;
  padding: 0 24rpx;
  font-size: 26rpx;
  font-weight: 600;
  box-shadow: 0 6rpx 20rpx rgba(255, 107, 107, 0.35);
  transition: all 0.2s ease;
  min-width: 90rpx;
  text-align: center;
}

.choose-btn:active {
  transform: scale(0.95);
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 107, 0.45);
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
