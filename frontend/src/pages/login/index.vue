<template>
  <view class="login-page">
    <view class="ambiance"></view>
    <view class="ambiance-soft"></view>

    <view class="brand">
      <text class="brand-name">家庭点餐</text>
      <text class="brand-line">把每一餐，留给家人</text>
    </view>

    <view class="login-panel">
      <text class="panel-title">微信授权登录</text>
      <text class="panel-desc">登录后加入家庭，即可点餐与接单</text>

      <view v-if="userInfo" class="profile">
        <image
          class="avatar"
          :src="userInfo.avatarUrl || '/static/icons/default-avatar.png'"
          mode="aspectFill"
        />
        <text class="nickname">{{ userInfo.nickname || '微信用户' }}</text>
      </view>

      <button
        v-if="!userInfo"
        class="btn btn-primary btn-block login-btn"
        open-type="getUserProfile"
        @click="onGetUserProfile"
        :loading="loading"
      >
        微信授权登录
      </button>

      <button
        v-else
        class="btn btn-primary btn-block login-btn"
        @click="onConfirmLogin"
        :loading="loading"
      >
        确认登录
      </button>
    </view>

    <text class="footer-tip">登录即表示同意服务条款</text>
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
    if (userManager.isLoggedIn()) {
      this.goToIndex()
    }
  },

  methods: {
    onGetUserProfile() {
      this.loading = true
      uni.getUserProfile({
        desc: '用于完善个人资料',
        success: (res) => {
          this.userInfo = {
            nickname: res.userInfo.nickName,
            avatarUrl: res.userInfo.avatarUrl
          }
          this.loading = false
        },
        fail: () => {
          uni.showToast({ title: '需要授权才能继续', icon: 'none' })
          this.loading = false
        }
      })
    },

    async onConfirmLogin() {
      if (!this.userInfo) return
      this.loading = true
      try {
        const loginRes = await new Promise((resolve, reject) => {
          uni.login({
            provider: 'weixin',
            success: (res) => resolve(res),
            fail: reject
          })
        })

        const res = await authApi.wxLogin({
          code: loginRes.code,
          nickname: this.userInfo.nickname,
          avatarUrl: this.userInfo.avatarUrl
        })

        userManager.saveUserInfo({
          ...res.user,
          isAdmin: !!res.user.isAdmin
        })
        if (res.token) {
          userManager.saveToken(res.token)
        }

        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => this.goToIndex(), 800)
      } catch (error) {
        console.error('登录失败:', error)
        uni.showToast({ title: '登录失败，请重试', icon: 'none' })
      } finally {
        this.loading = false
      }
    },

    goToIndex() {
      uni.switchTab({ url: '/pages/index/index' })
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 0 48rpx 80rpx;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  background: #F7F3EE;
}

.ambiance {
  position: absolute;
  top: -120rpx;
  left: -80rpx;
  right: -80rpx;
  height: 58vh;
  background: linear-gradient(165deg, #C56A42 0%, #B85C38 42%, #8F4528 100%);
  border-radius: 0 0 48% 48%;
  opacity: 0.92;
}

.ambiance-soft {
  position: absolute;
  top: 28vh;
  left: 10%;
  width: 80%;
  height: 220rpx;
  background: radial-gradient(ellipse, rgba(255, 251, 247, 0.35), transparent 70%);
  pointer-events: none;
}

.brand {
  position: relative;
  z-index: 1;
  padding-top: 22vh;
  margin-bottom: 56rpx;
}

.brand-name {
  display: block;
  font-size: 64rpx;
  font-weight: 700;
  color: #FFFBF7;
  letter-spacing: 4rpx;
  line-height: 1.2;
}

.brand-line {
  display: block;
  margin-top: 16rpx;
  font-size: 28rpx;
  color: rgba(255, 251, 247, 0.82);
  letter-spacing: 2rpx;
}

.login-panel {
  position: relative;
  z-index: 1;
  background: #FFFBF7;
  border-radius: 24rpx;
  padding: 48rpx 40rpx;
  box-shadow: 0 12rpx 40rpx rgba(42, 36, 32, 0.1);
}

.panel-title {
  display: block;
  font-size: 34rpx;
  font-weight: 600;
  color: #2A2420;
  margin-bottom: 8rpx;
}

.panel-desc {
  display: block;
  font-size: 24rpx;
  color: #9A9086;
  margin-bottom: 40rpx;
}

.profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 36rpx;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  margin-bottom: 16rpx;
  border: 4rpx solid #E8E0D6;
}

.nickname {
  font-size: 30rpx;
  font-weight: 600;
  color: #2A2420;
}

.login-btn {
  height: 92rpx;
  font-size: 30rpx;
}

.footer-tip {
  position: relative;
  z-index: 1;
  margin-top: auto;
  padding-top: 48rpx;
  text-align: center;
  font-size: 22rpx;
  color: #9A9086;
}
</style>
