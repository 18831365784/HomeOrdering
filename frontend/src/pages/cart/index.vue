<template>
  <view class="page">
    <view v-if="cartItems.length > 0" class="content page-with-bottom">
      <view class="list">
        <view class="cart-row" v-for="item in cartItems" :key="item.key || item.id">
          <view class="thumb">
            <SafeImage
              v-if="item.imageUrl"
              :src="item.imageUrl"
              imgClass="thumb-img"
              mode="aspectFill"
            />
            <view v-else class="thumb-empty">
              <AppIcon name="dish" :size="36" color="#D4C9BC" />
            </view>
          </view>

          <view class="info">
            <view class="name-row">
              <text class="name">{{ item.name }}</text>
              <text class="remove" @click="removeItem(item)">删除</text>
            </view>
            <view
              v-if="item.selectedOptions && Object.keys(item.selectedOptions).length > 0"
              class="opts"
            >
              <text
                v-for="(value, key) in item.selectedOptions"
                :key="key"
                class="opt"
              >{{ formatOptionDisplay(key, value) }}</text>
            </view>
            <view class="meta">
              <text class="price-small">¥{{ item.price }}</text>
              <QuantityStepper
                :value="item.quantity"
                :min="1"
                @change="onItemQtyChange(item, $event)"
              />
            </view>
          </view>
        </view>
      </view>

      <view class="checkout-block">
        <text class="block-title">结算信息</text>

        <view class="field">
          <text class="field-label">备注</text>
          <textarea
            class="textarea"
            v-model="remark"
            placeholder="口味、忌口等"
            maxlength="200"
          />
        </view>

        <view class="field">
          <text class="field-label">制作人</text>
          <view class="makers">
            <view
              v-for="member in familyMembers"
              :key="member.uuid"
              class="maker"
              :class="{ active: selectedMaker && selectedMaker.uuid === member.uuid }"
              @click="selectMaker(member)"
            >
              <image
                class="maker-avatar"
                :src="member.avatarUrl || '/static/icons/default-avatar.png'"
                mode="aspectFill"
              />
              <view class="maker-meta">
                <text class="maker-name">{{ member.nickname || '用户' }}</text>
                <text v-if="member.isAdmin" class="maker-tag">管理员</text>
              </view>
              <view v-if="selectedMaker && selectedMaker.uuid === member.uuid" class="check">
                <AppIcon name="check" :size="24" color="#B85C38" />
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <EmptyState
      v-else
      icon="empty-plate"
      title="购物车是空的"
      text="去点几道家常菜吧"
      action-text="去点餐"
      @action="goShopping"
    />

    <view v-if="cartItems.length > 0" class="bottom-bar">
      <view class="total">
        <text class="total-label">合计</text>
        <text class="price">¥{{ totalAmount }}</text>
      </view>
      <button class="btn btn-primary submit-btn" @click="submitOrder">提交订单</button>
    </view>
  </view>
</template>

<script>
import cartManager from '@/utils/cart.js'
import { orderApi, familyApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'
import SafeImage from '@/components/SafeImage.vue'
import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import QuantityStepper from '@/components/QuantityStepper.vue'

export default {
  components: { SafeImage, AppIcon, EmptyState, QuantityStepper },
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
      const total = this.cartItems.reduce(
        (sum, item) => sum + Number(item.price) * Number(item.quantity),
        0
      )
      return total.toFixed(2)
    }
  },

  onShow() {
    this.loadCart()
    this.loadFamilyMembers()
  },

  methods: {
    loadCart() {
      this.cartItems = cartManager.getCart()
    },

    async loadFamilyMembers() {
      try {
        const uuid = userManager.getUuid()
        if (!uuid) return
        const info = await familyApi.getFamilyInfo()
        if (info && info.members) {
          this.familyMembers = info.members
          const myInfo = info.members.find(m => m.uuid === uuid)
          if (myInfo) this.selectedMaker = myInfo
        }
      } catch (e) {
        console.error('加载家庭成员失败', e)
      }
    },

    selectMaker(member) {
      this.selectedMaker = member
    },

    onQtyChange(item, qty) {
      cartManager.updateQuantityByKey(item.key, qty)
      this.loadCart()
    },

    onItemQtyChange(item, qty) {
      this.onQtyChange(item, qty)
    },

    removeItem(item) {
      uni.showModal({
        title: '提示',
        content: '确定删除这个菜品吗？',
        success: (res) => {
          if (res.confirm) {
            cartManager.removeFromCart(item.key)
            this.loadCart()
          }
        }
      })
    },

    goShopping() {
      uni.switchTab({ url: '/pages/index/index' })
    },

    async submitOrder() {
      if (this.cartItems.length === 0) {
        uni.showToast({ title: '购物车为空', icon: 'none' })
        return
      }
      if (!this.selectedMaker || !this.selectedMaker.uuid) {
        uni.showToast({ title: '请选择制作人', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '提交中...' })
        const orderData = {
          remark: this.buildOrderRemark(),
          items: this.cartItems.map(item => ({
            dishId: item.id,
            quantity: item.quantity,
            unitPrice: item.price
          })),
          makerUuid: this.selectedMaker.uuid
        }
        await orderApi.create(orderData)
        cartManager.clearCart()
        this.remark = ''
        uni.hideLoading()
        uni.showModal({
          title: '订单提交成功',
          content: '已通知制作人，可在订单页查看进度',
          showCancel: false,
          success: () => {
            uni.switchTab({ url: '/pages/order/list' })
          }
        })
      } catch (error) {
        uni.hideLoading()
        console.error('提交订单失败:', error)
      }
    },

    formatOptionDisplay(key, value) {
      if (Array.isArray(value)) return `${key}: ${value.join(', ')}`
      if (typeof value === 'object' && value !== null) return `${key}: ${JSON.stringify(value)}`
      return `${key}: ${value}`
    },

    buildOrderRemark() {
      let remark = this.remark || ''
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

    formatOptionValue(value) {
      if (Array.isArray(value)) return value.join(', ')
      if (typeof value === 'object' && value !== null) return JSON.stringify(value)
      return String(value)
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

.content {
  padding: 16rpx 24rpx;
  padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.cart-row {
  display: flex;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
}

.thumb {
  width: 140rpx;
  height: 140rpx;
  border-radius: 14rpx;
  overflow: hidden;
  background: #F7F3EE;
  flex-shrink: 0;
}

:deep(.thumb-img) {
  width: 140rpx;
  height: 140rpx;
}

.thumb-empty {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.name-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16rpx;
}

.name {
  font-size: 28rpx;
  font-weight: 600;
  color: #2A2420;
  flex: 1;
}

.remove {
  font-size: 24rpx;
  color: #9A9086;
  flex-shrink: 0;
}

.opts {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.opt {
  font-size: 22rpx;
  color: #B85C38;
  background: rgba(184, 92, 56, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}

.checkout-block {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
}

.block-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #2A2420;
  margin-bottom: 24rpx;
}

.field {
  margin-bottom: 28rpx;
}

.field:last-child {
  margin-bottom: 0;
}

.field-label {
  display: block;
  font-size: 24rpx;
  color: #9A9086;
  margin-bottom: 12rpx;
}

.makers {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.maker {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  border-radius: 14rpx;
  background: #F7F3EE;
  border: 2rpx solid transparent;
}

.maker.active {
  border-color: #B85C38;
  background: rgba(184, 92, 56, 0.08);
}

.maker-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
}

.maker-meta {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.maker-name {
  font-size: 28rpx;
  color: #2A2420;
  font-weight: 500;
}

.maker-tag {
  font-size: 20rpx;
  color: #5C6B5A;
  background: rgba(92, 107, 90, 0.12);
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
}

.check {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.total {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  flex: 1;
}

.total-label {
  font-size: 26rpx;
  color: #6B6158;
}

.submit-btn {
  min-width: 240rpx;
  padding: 22rpx 36rpx;
}
</style>
