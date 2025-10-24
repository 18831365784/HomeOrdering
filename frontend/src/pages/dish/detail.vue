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
      
      <!-- 动态扩展选项 -->
      <view v-if="extOptions.length" class="options-card card">
        <text class="label">可选项</text>
        <view class="options-list">
          <view v-for="opt in extOptions" :key="opt.id" class="option-group">
            <text class="option-title">{{ opt.name }}<text v-if="opt.required" style="color:#E56C6C"> *</text></text>
            <view v-if="opt.selectionType === 'single' && Array.isArray(opt.choices)" class="chips">
              <view v-for="c in opt.choices" :key="c.id" class="chip" :class="{active: selected[opt.id]===c.id}" @click="selectSingle(opt.id, c.id)">
                <text>{{ c.name }}</text>
                <text v-if="c.price>0" class="chip-price">+¥{{ c.price }}</text>
              </view>
            </view>
            <view v-else-if="opt.selectionType === 'multiple' && Array.isArray(opt.choices)" class="chips">
              <view v-for="c in opt.choices" :key="c.id" class="chip" :class="{active: (selected[opt.id]||[]).includes(c.id)}" @click="toggleMultiple(opt.id, c.id)">
                <text>{{ c.name }}</text>
                <text v-if="c.price>0" class="chip-price">+¥{{ c.price }}</text>
              </view>
              <!-- 多选限制提示 -->
              <view v-if="opt.min > 0 || opt.max > 0" class="selection-hint">
                <text v-if="opt.min > 0 && opt.max > 0">至少选择{{ opt.min }}项，最多{{ opt.max }}项</text>
                <text v-else-if="opt.min > 0">至少选择{{ opt.min }}项</text>
                <text v-else-if="opt.max > 0">最多选择{{ opt.max }}项</text>
              </view>
            </view>
            <view v-else-if="opt.selectionType === 'input'" class="input-group">
              <input class="option-input" :placeholder="opt.placeholder || '请输入'" v-model="selected[opt.id]" />
            </view>
            <view v-else-if="opt.selectionType === 'number'" class="input-group">
              <input class="option-input" type="number" :placeholder="opt.placeholder || '请输入数字'" v-model.number="selected[opt.id]" />
              <text v-if="opt.unit" class="unit">{{ opt.unit }}</text>
            </view>
            <view v-else-if="opt.selectionType === 'boolean'" class="chips">
              <view class="chip" :class="{active: selected[opt.id]===true}" @click="selected[opt.id]=true">是</view>
              <view class="chip" :class="{active: selected[opt.id]===false}" @click="selected[opt.id]=false">否</view>
            </view>
            <view v-else class="input-group">
              <!-- 未知类型：给一个文本输入，逗号分隔 -->
              <input class="option-input" placeholder="可输入多个，用逗号分隔" v-model="selected[opt.id]" />
            </view>
          </view>
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
      quantity: 1,
      extOptions: [],
      selected: {}
    }
  },
  
  computed: {
    totalPrice() {
      if (!this.dish) return 0
      // 计算扩展项加价
      let extra = 0
      this.extOptions.forEach(opt => {
        if ((opt.selectionType === 'single' || opt.selectionType === 'multiple') && Array.isArray(opt.choices)) {
          if (opt.selectionType === 'single') {
            const cid = this.selected[opt.id]
            const ch = opt.choices.find(c => c.id === cid)
            if (ch && ch.price) extra += Number(ch.price)
          } else {
            const arr = this.selected[opt.id] || []
            arr.forEach(cid => {
              const ch = opt.choices.find(c => c.id === cid)
              if (ch && ch.price) extra += Number(ch.price)
            })
          }
        }
      })
      const base = Number(this.dish.price) + extra
      return (base * this.quantity).toFixed(2)
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
        // 解析扩展项
        const ext = dish.extensions
        let obj = null
        if (ext) {
          if (typeof ext === 'string') { try { obj = JSON.parse(ext) } catch(e){} } else if (typeof ext === 'object') { obj = ext }
        }
        const options = obj && Array.isArray(obj.options) ? obj.options : []
        // 规范化：补全selectionType
        this.extOptions = options.map(o => ({ selectionType: 'single', required: false, choices: [], ...o }))
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
    
    // 构造可读的已选项（用于购物车展示和订单备注）
    buildReadableSelected() {
      const out = {}
      this.extOptions.forEach(opt => {
        if (this.selected.hasOwnProperty(opt.id)) {
          const label = opt.name || opt.id
          if (opt.selectionType === 'single' && Array.isArray(opt.choices)) {
            const cid = this.selected[opt.id]
            const ch = opt.choices.find(c => c.id === cid)
            out[label] = ch ? ch.name : cid
          } else if (opt.selectionType === 'multiple' && Array.isArray(opt.choices)) {
            const arr = this.selected[opt.id] || []
            out[label] = arr.map(cid => {
              const ch = opt.choices.find(c => c.id === cid)
              return ch ? ch.name : cid
            })
          } else if (opt.selectionType === 'number' && opt.unit) {
            // 数字输入类型，包含单位
            out[label] = `${this.selected[opt.id]}${opt.unit}`
          } else {
            out[label] = this.selected[opt.id]
          }
        }
      })
      return out
    },

    // 计算包含加价的单份价格
    calcUnitPrice() {
      if (!this.dish) return 0
      let extra = 0
      this.extOptions.forEach(opt => {
        if ((opt.selectionType === 'single' || opt.selectionType === 'multiple') && Array.isArray(opt.choices)) {
          if (opt.selectionType === 'single') {
            const cid = this.selected[opt.id]
            const ch = opt.choices.find(c => c.id === cid)
            if (ch && ch.price) extra += Number(ch.price)
          } else {
            const arr = this.selected[opt.id] || []
            arr.forEach(cid => {
              const ch = opt.choices.find(c => c.id === cid)
              if (ch && ch.price) extra += Number(ch.price)
            })
          }
        }
      })
      return Number(this.dish.price) + extra
    },

    // 加入购物车
    addToCart() {
      // 校验扩展项
      if (!this.validateExtensions()) {
        return
      }
      
      const readable = this.buildReadableSelected()
      const unitPrice = this.calcUnitPrice()
      cartManager.addToCart(this.dish, this.quantity, readable, unitPrice)
      uni.showToast({
        title: '已添加到购物车',
        icon: 'success'
      })
      
      // 返回上一页
      setTimeout(() => {
        uni.navigateBack()
      }, 1000)
    },
    selectSingle(optId, choiceId) { 
      this.selected[optId] = choiceId
      this.$forceUpdate() // 强制更新视图
    },
    toggleMultiple(optId, choiceId) {
      const arr = Array.isArray(this.selected[optId]) ? [...this.selected[optId]] : []
      const idx = arr.indexOf(choiceId)
      if (idx >= 0) { 
        arr.splice(idx, 1) 
      } else { 
        arr.push(choiceId) 
      }
      this.selected[optId] = arr
      this.$forceUpdate() // 强制更新视图
    },
    
    // 校验扩展项
    validateExtensions() {
      for (const opt of this.extOptions) {
        // 必填校验
        if (opt.required) {
          const value = this.selected[opt.id]
          if (!value || (Array.isArray(value) && value.length === 0) || value === '') {
            // 根据类型使用不同的提示词
            let promptText = ''
            if (opt.selectionType === 'input' || opt.selectionType === 'number') {
              promptText = `"${opt.name}"为必填项，请填写`
            } else {
              promptText = `"${opt.name}"为必选项，请选择`
            }
            
            uni.showToast({
              title: promptText,
              icon: 'none',
              duration: 2000
            })
            return false
          }
        }
        
        // 多选类型的最少最多校验
        if (opt.selectionType === 'multiple' && Array.isArray(opt.choices)) {
          const selectedCount = Array.isArray(this.selected[opt.id]) ? this.selected[opt.id].length : 0
          
          // 最少校验
          if (opt.min !== undefined && opt.min !== null && opt.min > 0 && selectedCount < opt.min) {
            uni.showToast({
              title: `"${opt.name}"至少需要选择${opt.min}项`,
              icon: 'none',
              duration: 2000
            })
            return false
          }
          
          // 最多校验
          if (opt.max !== undefined && opt.max !== null && opt.max > 0 && selectedCount > opt.max) {
            uni.showToast({
              title: `"${opt.name}"最多只能选择${opt.max}项`,
              icon: 'none',
              duration: 2000
            })
            return false
          }
        }
      }
      return true
    },
    
    // 立即下单
    buyNow() {
      // 校验扩展项
      if (!this.validateExtensions()) {
        return
      }
      
      // 清空购物车，只购买当前菜品
      cartManager.clearCart()
      const readable = this.buildReadableSelected()
      const unitPrice = this.calcUnitPrice()
      cartManager.addToCart(this.dish, this.quantity, readable, unitPrice)
      
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
  background-color: #EFE7DD;
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
  color: #2E2A27;
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
  color: #6A625B;
  font-weight: bold;
}

.content {
  font-size: 28rpx;
  color: #6A625B;
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
  border: 2rpx solid #E2D8CC;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #6A625B;
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
  box-shadow: 0 -8rpx 24rpx rgba(123, 91, 68, 0.06);
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

/* 扩展选项样式 */
.options-card {
  margin: 0 20rpx 20rpx;
  padding: 24rpx;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.option-title {
  font-size: 30rpx;
  color: #2E2A27;
  font-weight: bold;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.chip {
  padding: 16rpx 24rpx;
  border: 2rpx solid #E2D8CC;
  border-radius: 24rpx;
  background: #F6F3EF;
  color: #6A625B;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  transition: all 0.2s ease;
  cursor: pointer;
}

.chip.active {
  background: linear-gradient(135deg, #7B5B44 0%, #9F7A5A 100%);
  color: #ffffff;
  border-color: #7B5B44;
  box-shadow: 0 4rpx 12rpx rgba(123, 91, 68, 0.25);
}

.chip-price {
  font-size: 22rpx;
  opacity: 0.8;
}

.option-input {
  background: #F6F3EF;
  border: 2rpx solid #E2D8CC;
  border-radius: 16rpx;
  padding: 24rpx 20rpx;
  font-size: 28rpx;
  color: #2E2A27;
  width: 100%;
  min-height: 80rpx;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.option-input:focus {
  border-color: #7B5B44;
  background: #ffffff;
  box-shadow: 0 0 0 4rpx rgba(123, 91, 68, 0.1);
}

.input-group {
  display: flex;
  align-items: center;
  gap: 12rpx;
  width: 100%;
}

.unit {
  font-size: 24rpx;
  color: #A39A92;
  margin-left: 12rpx;
  flex-shrink: 0;
}

.selection-hint {
  width: 100%;
  margin-top: 8rpx;
  padding: 8rpx 12rpx;
  background: transparent;
  border-radius: 8rpx;
  text-align: left;
}

.selection-hint text {
  font-size: 22rpx;
  color: #A39A92;
  line-height: 1.4;
}
</style>
