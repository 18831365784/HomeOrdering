<template>
  <view class="login-container">
    <!-- 背景粒子/渐变 -->
    <view class="bg-gradient"></view>
    
    <!-- Logo区域 -->
    <view class="brand-section">
      <view class="logo-wrapper">
        <text class="logo-icon">🍽️</text>
      </view>
      <text class="brand-title">家庭点餐</text>
      <text class="brand-subtitle">温馨每一餐</text>
    </view>
    
    <!-- 登录卡片 -->
    <view class="login-card card">
      <text class="card-title">微信授权登录</text>
      
      <!-- 用户信息展示 -->
      <view v-if="userInfo" class="user-info-section">
        <image 
          class="avatar" 
          :src="userInfo.avatarUrl || '/static/icons/default-avatar.png'" 
          mode="aspectFill"
        />
        <view class="nickname-row">
          <text class="nickname">{{ userInfo.nickname || '微信用户' }}</text>
          <text class="welcome-text">欢迎使用家庭点餐</text>
        </view>
      </view>
      
      <!-- 授权按钮 -->
      <button 
        v-if="!userInfo" 
        class="wx-login-btn primary-bg"
        open-type="getUserProfile"
        @click="onGetUserProfile"
        :loading="loading"
      >
        <text>微信授权登录</text>
      </button>
      
      <!-- 确认按钮 -->
      <button 
        v-if="userInfo" 
        class="confirm-btn primary-bg"
        @click="onConfirmLogin"
        :loading="loading"
      >
        <text>确认登录</text>
      </button>
      
      <!-- 加载提示 -->
      <view v-if="loading" class="loading-tip">
        <text>登录中...</text>
      </view>
    </view>
    
    <!-- 底部说明 -->
    <view class="footer-tip">
      <text>登录即表示同意我们的服务条款</text>
    </view>
  </view>
</template>

<script>
import { authApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'

export default {
  data() {
    return {
      loading: false,
      userInfo: null
    }
  },
  
  onLoad() {
    // 检查是否已登录
    if (userManager.isLoggedIn()) {
      // 已登录，直接跳转
      this.goToIndex()
    }
  },
  
  methods: {
    // 获取用户昵称头像
    onGetUserProfile() {
      this.loading = true
      uni.getUserProfile({
        desc: '用于完善个人资料',
        success: (res) => {
          console.log('获取用户信息成功:', res)
          this.userInfo = {
            nickname: res.userInfo.nickName,
            avatarUrl: res.userInfo.avatarUrl
          }
          this.loading = false
        },
        fail: (err) => {
          console.error('获取用户信息失败:', err)
          uni.showToast({
            title: '需要授权才能继续',
            icon: 'none'
          })
          this.loading = false
        }
      })
    },
    
    // 确认登录
    async onConfirmLogin() {
      if (!this.userInfo) return
      
      this.loading = true
      
      try {
        // 1. 先获取微信 code
        const loginRes = await new Promise((resolve, reject) => {
          uni.login({
            provider: 'weixin',
            success: (res) => resolve(res),
            fail: reject
          })
        })
        
        // 2. 调用后端登录
        const res = await authApi.wxLogin({
          code: loginRes.code,
          nickname: this.userInfo.nickname,
          avatarUrl: this.userInfo.avatarUrl
        })
        
        console.log('登录成功:', res)
        
        // 3. 保存用户信息
        userManager.saveUserInfo({
          ...res.user,
          isAdmin: !!res.user.isAdmin
        })
        if (res.token) {
          userManager.saveToken(res.token)
        }
        
        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })
        
        // 4. 跳转首页
        setTimeout(() => {
          this.goToIndex()
        }, 1000)
        
      } catch (error) {
        console.error('登录失败:', error)
        uni.showToast({
          title: '登录失败，请重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    // 跳转到首页
    goToIndex() {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 40rpx;
  position: relative;
  overflow: hidden;
}

/* 蓝色渐变背景 */
.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 50vh;
  background: linear-gradient(135deg, #1d4ed8 0%, #3b82f6 50%, #60a5fa 100%);
  border-radius: 0 0 100rpx 100rpx;
}

/* Logo区域 */
.brand-section {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 80rpx;
}

.logo-wrapper {
  width: 160rpx;
  height: 160rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
  backdrop-filter: blur(10px);
}

.logo-icon {
  font-size: 80rpx;
}

.brand-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 12rpx;
}

.brand-subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 登录卡片 */
.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 48rpx 40rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.card-title {
  display: block;
  text-align: center;
  font-size: 36rpx;
  font-weight: bold;
  color: #1d4ed8;
  margin-bottom: 40rpx;
}

/* 用户信息展示 */
.user-info-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32rpx;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  border: 4rpx solid #e0e7ff;
  margin-bottom: 20rpx;
}

.nickname-row {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.nickname {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 8rpx;
}

.welcome-text {
  font-size: 24rpx;
  color: #666666;
}

/* 微信登录按钮 */
.wx-login-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  width: 100%;
  height: 96rpx;
  border-radius: 48rpx;
  background: #07c160;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
  margin-bottom: 24rpx;
}

.wx-login-btn::after {
  border: none;
}

.secondary-btn {
  background: #ffffff;
  color: #333333;
  border: 2rpx solid #e5e5e5;
}

.wx-icon {
  width: 48rpx;
  height: 48rpx;
}

/* 确认按钮 */
.confirm-btn {
  width: 100%;
  height: 96rpx;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
}

.confirm-btn::after {
  border: none;
}

/* 加载提示 */
.loading-tip {
  text-align: center;
  margin-top: 24rpx;
  color: #999999;
  font-size: 24rpx;
}

/* 底部说明 */
.footer-tip {
  position: absolute;
  bottom: 60rpx;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 22rpx;
  color: #999999;
}

</style>