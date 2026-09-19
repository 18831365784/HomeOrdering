<template>
  <view class="page">
    <view v-if="dish" class="page-with-bottom">
      <view class="banner">
        <SafeImage
          v-if="dish.imageUrl"
          :src="dish.imageUrl"
          mode="aspectFill"
          imgClass="banner-img"
        />
        <view v-else class="banner-empty">
          <AppIcon name="dish" :size="64" color="#D4C9BC" />
        </view>
      </view>

      <view class="panel">
        <view class="title-row">
          <text class="dish-name">{{ dish.name }}</text>
          <text class="sales">已点 {{ dish.orderCount || 0 }}</text>
        </view>
        <text class="price">¥{{ dish.price }}</text>
        <text class="desc">{{ dish.description || '暂无简介' }}</text>
      </view>

      <view v-if="extOptions.length" class="panel">
        <text class="panel-title">可选项</text>
        <view v-for="opt in extOptions" :key="opt.id" class="opt-group">
          <text class="opt-name">
            {{ opt.name }}
            <text v-if="opt.required" class="req">*</text>
          </text>

          <view v-if="opt.selectionType === 'single' && Array.isArray(opt.choices)" class="chips">
            <view
              v-for="c in opt.choices"
              :key="c.id"
              class="chip"
              :class="{ active: selected[opt.id] === c.id }"
              @click="selectSingle(opt.id, c.id)"
            >
              <text>{{ c.name }}</text>
              <text v-if="c.price > 0" class="chip-price">+¥{{ c.price }}</text>
            </view>
          </view>

          <view v-else-if="opt.selectionType === 'multiple' && Array.isArray(opt.choices)" class="chips">
            <view
              v-for="c in opt.choices"
              :key="c.id"
              class="chip"
              :class="{ active: (selected[opt.id] || []).includes(c.id) }"
              @click="toggleMultiple(opt.id, c.id)"
            >
              <text>{{ c.name }}</text>
              <text v-if="c.price > 0" class="chip-price">+¥{{ c.price }}</text>
            </view>
            <view v-if="opt.min > 0 || opt.max > 0" class="hint">
              <text v-if="opt.min > 0 && opt.max > 0">至少选{{ opt.min }}项，最多{{ opt.max }}项</text>
              <text v-else-if="opt.min > 0">至少选{{ opt.min }}项</text>
              <text v-else-if="opt.max > 0">最多选{{ opt.max }}项</text>
            </view>
          </view>

          <view v-else-if="opt.selectionType === 'input'" class="input-group">
            <input class="input" :placeholder="opt.placeholder || '请输入'" v-model="selected[opt.id]" />
          </view>

          <view v-else-if="opt.selectionType === 'number'" class="input-group">
            <input
              class="input"
              type="number"
              :placeholder="opt.placeholder || '请输入数字'"
              v-model.number="selected[opt.id]"
            />
            <text v-if="opt.unit" class="unit">{{ opt.unit }}</text>
          </view>

          <view v-else-if="opt.selectionType === 'boolean'" class="chips">
            <view class="chip" :class="{ active: selected[opt.id] === true }" @click="selected[opt.id] = true">是</view>
            <view class="chip" :class="{ active: selected[opt.id] === false }" @click="selected[opt.id] = false">否</view>
          </view>

          <view v-else class="input-group">
            <input class="input" placeholder="可输入多个，用逗号分隔" v-model="selected[opt.id]" />
          </view>
        </view>
      </view>

      <view class="panel qty-panel">
        <text class="panel-title">数量</text>
        <QuantityStepper :value="quantity" :min="1" @change="onQtyChange" />
      </view>
    </view>

    <view v-else class="loading">
      <text class="text-muted">加载中...</text>
    </view>

    <view v-if="dish" class="bottom-bar">
      <view class="total">
        <text class="total-label">小计</text>
        <text class="price">¥{{ totalPrice }}</text>
      </view>
      <button class="btn btn-ghost cart-btn" @click="addToCart">加购</button>
      <button class="btn btn-primary buy-btn" @click="buyNow">立即下单</button>
    </view>
  </view>
</template>

<script>
import { dishApi } from '@/utils/api.js'
import SafeImage from '@/components/SafeImage.vue'
import AppIcon from '@/components/AppIcon.vue'
import QuantityStepper from '@/components/QuantityStepper.vue'
import cartManager from '@/utils/cart.js'

export default {
  components: { SafeImage, AppIcon, QuantityStepper },
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
      return ((Number(this.dish.price) + extra) * this.quantity).toFixed(2)
    }
  },

  onLoad(options) {
    this.dishId = options.id
    this.loadDishDetail()
  },

  methods: {
    async loadDishDetail() {
      try {
        uni.showLoading({ title: '加载中...' })
        const dish = await dishApi.getDetail(this.dishId)
        this.dish = dish
        const ext = dish.extensions
        let obj = null
        if (ext) {
          if (typeof ext === 'string') {
            try { obj = JSON.parse(ext) } catch (e) {}
          } else if (typeof ext === 'object') {
            obj = ext
          }
        }
        const options = obj && Array.isArray(obj.options) ? obj.options : []
        this.extOptions = options.map(o => ({
          selectionType: 'single',
          required: false,
          choices: [],
          ...o
        }))
      } catch (error) {
        console.error('加载菜品详情失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },

    buildReadableSelected() {
      const out = {}
      this.extOptions.forEach(opt => {
        if (Object.prototype.hasOwnProperty.call(this.selected, opt.id)) {
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
            out[label] = `${this.selected[opt.id]}${opt.unit}`
          } else {
            out[label] = this.selected[opt.id]
          }
        }
      })
      return out
    },

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

    addToCart() {
      if (!this.validateExtensions()) return
      const readable = this.buildReadableSelected()
      const unitPrice = this.calcUnitPrice()
      cartManager.addToCart(this.dish, this.quantity, readable, unitPrice)
      uni.showToast({ title: '已加入购物车', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 800)
    },

    selectSingle(optId, choiceId) {
      this.selected[optId] = choiceId
      this.$forceUpdate()
    },

    toggleMultiple(optId, choiceId) {
      const arr = Array.isArray(this.selected[optId]) ? [...this.selected[optId]] : []
      const idx = arr.indexOf(choiceId)
      if (idx >= 0) arr.splice(idx, 1)
      else arr.push(choiceId)
      this.selected[optId] = arr
      this.$forceUpdate()
    },

    validateExtensions() {
      for (const opt of this.extOptions) {
        if (opt.required) {
          const value = this.selected[opt.id]
          if (!value || (Array.isArray(value) && value.length === 0) || value === '') {
            const promptText = (opt.selectionType === 'input' || opt.selectionType === 'number')
              ? `"${opt.name}"为必填项，请填写`
              : `"${opt.name}"为必选项，请选择`
            uni.showToast({ title: promptText, icon: 'none', duration: 2000 })
            return false
          }
        }
        if (opt.selectionType === 'multiple' && Array.isArray(opt.choices)) {
          const selectedCount = Array.isArray(this.selected[opt.id]) ? this.selected[opt.id].length : 0
          if (opt.min !== undefined && opt.min !== null && opt.min > 0 && selectedCount < opt.min) {
            uni.showToast({ title: `"${opt.name}"至少需要选择${opt.min}项`, icon: 'none' })
            return false
          }
          if (opt.max !== undefined && opt.max !== null && opt.max > 0 && selectedCount > opt.max) {
            uni.showToast({ title: `"${opt.name}"最多只能选择${opt.max}项`, icon: 'none' })
            return false
          }
        }
      }
      return true
    },

    buyNow() {
      if (!this.validateExtensions()) return
      cartManager.clearCart()
      const readable = this.buildReadableSelected()
      const unitPrice = this.calcUnitPrice()
      cartManager.addToCart(this.dish, this.quantity, readable, unitPrice)
      uni.switchTab({ url: '/pages/cart/index' })
    },

    onQtyChange(v) {
      this.quantity = v
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100%;
  background: #F7F3EE;
  box-sizing: border-box;
}

.banner {
  width: 100%;
  height: 480rpx;
  background: #E8E0D6;
}

:deep(.banner-img) {
  width: 100%;
  height: 480rpx;
}

.banner-empty {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.panel {
  margin: 20rpx 24rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.dish-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #2A2420;
  flex: 1;
}

.sales {
  font-size: 22rpx;
  color: #9A9086;
  margin-top: 8rpx;
}

.price {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #B85C38;
  margin-bottom: 16rpx;
}

.desc {
  font-size: 26rpx;
  color: #6B6158;
  line-height: 1.6;
}

.panel-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #2A2420;
  margin-bottom: 20rpx;
}

.opt-group {
  margin-bottom: 28rpx;
}

.opt-group:last-child {
  margin-bottom: 0;
}

.opt-name {
  display: block;
  font-size: 26rpx;
  color: #6B6158;
  margin-bottom: 12rpx;
}

.req {
  color: #B33A2B;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.chip {
  padding: 14rpx 22rpx;
  border: 2rpx solid #E8E0D6;
  border-radius: 12rpx;
  background: #F7F3EE;
  color: #6B6158;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.chip.active {
  background: rgba(184, 92, 56, 0.1);
  color: #B85C38;
  border-color: #B85C38;
  font-weight: 600;
}

.chip-price {
  font-size: 22rpx;
  opacity: 0.85;
}

.hint {
  width: 100%;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #9A9086;
}

.input-group {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.unit {
  font-size: 24rpx;
  color: #9A9086;
  flex-shrink: 0;
}

.qty-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.qty-panel .panel-title {
  margin-bottom: 0;
}

.loading {
  padding: 200rpx 0;
  text-align: center;
}

.total {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.total-label {
  font-size: 22rpx;
  color: #9A9086;
}

.cart-btn {
  padding: 20rpx 28rpx;
}

.buy-btn {
  padding: 20rpx 36rpx;
  min-width: 200rpx;
}
</style>
