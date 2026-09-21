<template>
  <view class="page">
    <view class="search-bar">
      <view class="search-field">
        <AppIcon name="search" :size="32" color="#9A9086" />
        <input
          class="search-input"
          placeholder="搜索菜品"
          placeholder-class="search-ph"
          v-model="searchKeyword"
          @input="onSearch"
        />
      </view>
    </view>

    <view class="main">
      <scroll-view class="sidebar" scroll-y>
        <view
          class="cat-item"
          :class="{ active: currentCategory === '' }"
          @click="selectCategory('')"
        >
          <text class="cat-text">全部</text>
        </view>
        <view
          v-for="cat in categories"
          :key="cat"
          class="cat-item"
          :class="{ active: currentCategory === cat }"
          @click="selectCategory(cat)"
        >
          <SafeImage
            v-if="isImageUrl(categoryIcons[cat])"
            imgClass="cat-img"
            :src="categoryIcons[cat]"
            mode="aspectFill"
          />
          <text class="cat-text">{{ cat }}</text>
        </view>
      </scroll-view>

      <scroll-view class="dish-pane" scroll-y>
        <view
          class="dish-row"
          v-for="dish in filteredDishes"
          :key="dish.id"
          @click="goToDetail(dish.id)"
        >
          <view class="dish-thumb">
            <SafeImage
              v-if="dish.imageUrl"
              imgClass="dish-thumb-img"
              :src="dish.imageUrl"
              mode="aspectFill"
            />
            <view v-else class="thumb-empty">
              <AppIcon name="dish" :size="40" color="#D4C9BC" />
            </view>
          </view>

          <view class="dish-body">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-desc">{{ dish.description || '暂无简介' }}</text>
            <view class="dish-foot">
              <view class="price-row">
                <text class="price">¥{{ dish.price }}</text>
                <text class="sales">已点 {{ dish.orderCount || 0 }}</text>
              </view>
              <view class="dish-action" @click.stop>
                <button
                  v-if="needChoose(dish)"
                  class="btn btn-primary btn-sm"
                  @click="goToDetail(dish.id)"
                >
                  选规格
                </button>
                <view v-else class="add-btn" @click="addToCart(dish)">
                  <AppIcon name="plus" :size="28" color="#FFFFFF" />
                </view>
              </view>
            </view>
          </view>
        </view>

        <EmptyState
          v-if="filteredDishes.length === 0"
          icon="empty-plate"
          text="暂无菜品"
        />
      </scroll-view>
    </view>
  </view>
</template>

<script>
import { dishApi, categoryApi, familyApi } from '@/utils/api.js'
import SafeImage from '@/components/SafeImage.vue'
import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import cartManager from '@/utils/cart.js'
import userManager from '@/utils/user.js'

export default {
  components: { SafeImage, AppIcon, EmptyState },
  data() {
    return {
      dishes: [],
      categories: [],
      currentCategory: '',
      searchKeyword: '',
      filteredDishes: [],
      isAdmin: false,
      categoryIcons: {}
    }
  },

  onLoad() {
    this.checkAdminStatus()
    this.loadCategories()
    this.loadDishes()
    uni.$on('userInfoUpdated', this.onUserInfoUpdated)
    uni.$on('categoryUpdated', this.onCategoryUpdated)
    uni.$on('dishUpdated', this.onDishUpdated)
  },

  onUnload() {
    uni.$off('userInfoUpdated', this.onUserInfoUpdated)
    uni.$off('categoryUpdated', this.onCategoryUpdated)
    uni.$off('dishUpdated', this.onDishUpdated)
  },

  onShow() {
    this.checkAdminStatus()
    this.loadCategories()
    this.loadDishes()
  },

  methods: {
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

    isImageUrl(v) {
      if (!v || typeof v !== 'string') return false
      return v.startsWith('http://') || v.startsWith('https://') || v.startsWith('/uploads/')
    },

    async loadCategories() {
      try {
        const list = await categoryApi.getList(1)
        this.categories = list.map(c => c.name)
        const icons = {}
        list.forEach(c => {
          if (c.iconUrl) icons[c.name] = c.iconUrl
        })
        this.categoryIcons = icons
      } catch (e) {
        console.error('加载分类失败', e)
      }
    },

    onUserInfoUpdated() {
      this.checkAdminStatus()
    },

    onCategoryUpdated() {
      this.loadCategories()
      this.loadDishes()
    },

    onDishUpdated() {
      this.loadDishes()
    },

    async checkAdminStatus() {
      try {
        this.isAdmin = !!(await familyApi.isFamilyAdmin())
        const userInfo = userManager.getUserInfo() || {}
        userManager.saveUserInfo({ ...userInfo, isAdmin: this.isAdmin })
      } catch (e) {
        this.isAdmin = userManager.isAdmin()
      }
    },

    selectCategory(category) {
      this.currentCategory = category
      this.filterDishes()
    },

    filterDishes() {
      let result = this.dishes
      if (this.currentCategory) {
        result = result.filter(dish => dish.category === this.currentCategory)
      }
      if (this.searchKeyword) {
        const kw = this.searchKeyword
        result = result.filter(dish =>
          dish.name.includes(kw) ||
          (dish.description && dish.description.includes(kw))
        )
      }
      this.filteredDishes = result
    },

    async loadDishes() {
      try {
        uni.showLoading({ title: '加载中...' })
        const dishes = await dishApi.getList(1)
        this.dishes = dishes
        this.filterDishes()
      } catch (error) {
        console.error('加载菜品失败:', error)
      } finally {
        uni.hideLoading()
      }
    },

    onSearch() {
      this.filterDishes()
    },

    goToDetail(dishId) {
      uni.navigateTo({ url: `/pages/dish/detail?id=${dishId}` })
    },

    addToCart(dish) {
      cartManager.addToCart(dish, 1)
      uni.showToast({ title: '已加入购物车', icon: 'success' })
    }
  }
}
</script>

<style scoped>
.page {
  /* 相对页面内容区铺满（内容区本身在导航栏下方），不要用 100vh */
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  background: #F7F3EE;
  box-sizing: border-box;
}

.search-bar {
  padding: 12rpx 24rpx 16rpx;
  background: #F7F3EE;
  flex-shrink: 0;
}

.search-field {
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
  border: 1rpx solid #E8E0D6;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #2A2420;
}

.search-ph {
  color: #9A9086;
}

.main {
  flex: 1;
  display: flex;
  min-height: 0;
}

.sidebar {
  width: 168rpx;
  background: transparent;
  height: 100%;
  border-right: 1rpx solid #E8E0D6;
}

.cat-item {
  padding: 28rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  position: relative;
}

.cat-item.active {
  background: #F7F3EE;
}

.cat-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6rpx;
  height: 36rpx;
  background: #B85C38;
  border-radius: 0 4rpx 4rpx 0;
}

.cat-item.active .cat-text {
  color: #B85C38;
  font-weight: 600;
}

:deep(.cat-img) {
  width: 44rpx;
  height: 44rpx;
  border-radius: 10rpx;
}

.cat-text {
  font-size: 24rpx;
  color: #6B6158;
  text-align: center;
  line-height: 1.3;
}

.dish-pane {
  flex: 1;
  height: 100%;
  padding: 16rpx 20rpx;
  box-sizing: border-box;
}

.dish-row {
  display: flex;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
}

.dish-thumb {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
  background: #F7F3EE;
}

:deep(.dish-thumb-img) {
  width: 160rpx;
  height: 160rpx;
}

.thumb-empty {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dish-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.dish-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #2A2420;
  margin-bottom: 6rpx;
}

.dish-desc {
  font-size: 24rpx;
  color: #9A9086;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex: 1;
}

.dish-foot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: 12rpx;
  gap: 12rpx;
}

.price-row {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.price {
  font-size: 32rpx;
  font-weight: 700;
  color: #B85C38;
}

.sales {
  font-size: 22rpx;
  color: #9A9086;
}

.add-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  background: #B85C38;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-btn:active {
  background: #8F4528;
}
</style>
