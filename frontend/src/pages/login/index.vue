<template>
  <view class="login-page">
    <view class="hero" :style="{ paddingTop: heroPadTop }">
      <text class="brand-name">家庭点餐</text>
      <text class="brand-line">把每一餐，留给家人</text>
    </view>

    <view class="login-panel">
      <text class="panel-title">{{ panelTitle }}</text>
      <text class="panel-desc">{{ panelDesc }}</text>

      <view class="profile">
        <button
          v-if="needProfile"
          class="avatar-btn"
          hover-class="none"
          open-type="chooseAvatar"
          @chooseavatar="onChooseAvatar"
        >
          <image class="avatar" :src="avatarSrc" mode="aspectFill" />
          <view class="avatar-badge">
            <AppIcon name="camera" :size="22" color="#FFFFFF" />
          </view>
        </button>
        <image v-else class="avatar" :src="avatarSrc" mode="aspectFill" />

        <text v-if="!needProfile" class="nickname">{{ displayName }}</text>
        <text v-if="needProfile" class="profile-hint" @click="chooseAlbumAvatar">
          点头像用微信头像，或点这里从相册选
        </text>
        <input
          v-if="needProfile"
          class="input nickname-input"
          type="nickname"
          maxlength="20"
          placeholder="点击填写，可选用微信昵称"
          @blur="onNicknameBlur"
          @input="onNicknameInput"
          @nicknamereview="onNicknameReview"
        />
      </view>

      <button
        class="btn btn-primary btn-block login-btn"
        :loading="loading"
        @click="onPrimaryTap"
      >
        {{ primaryText }}
      </button>
    </view>

    <text class="footer-tip">登录即表示同意服务条款</text>
  </view>
</template>

<script>
import { authApi, fileApi, userApi, resolveMediaUrl } from '@/utils/api.js'
import userManager from '@/utils/user.js'
import AppIcon from '@/components/AppIcon.vue'

const PLACEHOLDER_NICKNAME = '微信用户'
const PLACEHOLDER_AVATAR_ID =
  'POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVgKSyHlibeiaNsUpoladAKQw1ia9F4gZibXsweasXqibicw'
const DEFAULT_AVATAR = '/static/icons/default-avatar.png'

export default {
  components: { AppIcon },
  data() {
    return {
      loading: false,
      identifying: false,
      needProfile: false,
      readyToEnter: false,
      nickname: '',
      avatarPreview: '',
      avatarTempPath: '',
      heroPadTop: '96px'
    }
  },

  computed: {
    panelTitle() {
      if (this.readyToEnter) return '欢迎回来'
      if (this.needProfile) return '完善资料'
      return '微信登录'
    },
    panelDesc() {
      if (this.readyToEnter) return ''
      if (this.needProfile) return '选择头像并填写昵称，方便家人认出你'
      return '登录后加入家庭，即可点餐与接单'
    },
    displayName() {
      if (this.identifying) return '加载中…'
      if (this.hasUsableNickname(this.nickname)) return this.nickname
      return '微信用户'
    },
    avatarSrc() {
      return this.avatarPreview || DEFAULT_AVATAR
    },
    primaryText() {
      if (this.identifying) return '正在登录'
      if (this.readyToEnter) return '进入'
      if (this.needProfile) return '完成'
      return '微信登录'
    }
  },

  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      const bar = sys.statusBarHeight || 20
      this.heroPadTop = bar + 36 + 'px'
    } catch (e) {}

    if (userManager.isLoggedIn()) {
      this.goToIndex()
      return
    }
    if (userManager.consumeManualLogout()) {
      return
    }
    this.identifyAccount()
  },

  methods: {
    hasUsableNickname(name) {
      const value = (name || '').trim()
      return !!value && value !== PLACEHOLDER_NICKNAME
    },

    displayAvatar(url) {
      if (!url || url.includes(PLACEHOLDER_AVATAR_ID)) return ''
      return resolveMediaUrl(url)
    },

    pickUser(res) {
      if (!res || typeof res !== 'object') return {}
      if (res.user && typeof res.user === 'object') return res.user
      return res
    },

    shouldEnterDirectly(res, user) {
      if (res && res.profileCompleted === true) return true
      if (res && res.newUser === true) return false
      return this.hasUsableNickname(user && user.nickname)
    },

    wxLoginCode() {
      return new Promise((resolve, reject) => {
        const attempt = (retry) => {
          uni.login({
            timeout: 10000,
            success: (res) => {
              if (res && res.code) {
                resolve(res)
                return
              }
              if (retry) attempt(false)
              else reject(new Error('未获得登录码'))
            },
            fail: (err) => {
              if (retry) attempt(false)
              else reject(err)
            }
          })
        }
        attempt(true)
      })
    },

    onPrimaryTap() {
      if (this.identifying || this.loading) return
      if (this.readyToEnter) {
        this.goToIndex()
        return
      }
      if (this.needProfile) {
        this.submitProfile()
        return
      }
      this.identifyAccount()
    },

    async identifyAccount() {
      this.loading = true
      this.identifying = true
      try {
        const loginRes = await this.wxLoginCode()
        const res = await authApi.wxLogin({
          code: loginRes.code
        })
        const user = this.pickUser(res)
        this.saveSession(res, user)

        this.nickname = (user.nickname || '').trim()
        this.avatarPreview = this.displayAvatar(user.avatarUrl)
        this.avatarTempPath = ''

        if (this.shouldEnterDirectly(res, user)) {
          this.readyToEnter = true
          this.needProfile = false
          return
        }

        this.readyToEnter = false
        this.needProfile = true
      } catch (error) {
        console.error('登录失败:', error)
        const msg = (error && (error.errMsg || error.message)) || ''
        uni.showToast({
          title: /timeout/i.test(msg) ? '登录超时，请重试' : '登录失败，请重试',
          icon: 'none'
        })
      } finally {
        this.identifying = false
        this.loading = false
      }
    },

    saveSession(res, user) {
      const profile = user || this.pickUser(res)
      userManager.saveUserInfo({
        ...profile,
        isAdmin: !!profile.isAdmin
      })
      if (res && res.token) {
        userManager.saveToken(res.token)
      }
    },

    onChooseAvatar(e) {
      const url = this.readAvatarUrl(e)
      if (!url) return
      this.avatarPreview = url
      this.avatarTempPath = url
    },

    readAvatarUrl(e) {
      const detail = (e && e.detail) || (e && e.mp && e.mp.detail) || {}
      return detail.avatarUrl || ''
    },

    chooseAlbumAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const path = res.tempFilePaths && res.tempFilePaths[0]
          if (!path) return
          this.avatarPreview = path
          this.avatarTempPath = path
        }
      })
    },

    onNicknameInput(e) {
      this.nickname = ((e.detail && e.detail.value) || '').trimStart()
    },

    onNicknameBlur(e) {
      this.nickname = this.readNickname(e)
    },

    onNicknameReview(e) {
      const pass = e && e.detail && (e.detail.pass === undefined || e.detail.pass)
      if (pass) {
        const value = this.readNickname(e)
        if (value) this.nickname = value
      }
    },

    readNickname(e) {
      const detail = (e && e.detail) || {}
      return ((detail.value != null ? detail.value : this.nickname) || '').trim()
    },

    async submitProfile() {
      const nickname = (this.nickname || '').trim()
      if (!this.hasUsableNickname(nickname)) {
        uni.showToast({ title: '请填写昵称，点击输入框可选用微信昵称', icon: 'none' })
        return
      }
      this.loading = true
      try {
        await userApi.updateProfile({ nickname })
        userManager.saveUserInfo({
          ...userManager.getUserInfo(),
          nickname
        })

        if (this.avatarTempPath) {
          try {
            const imageUrl = await fileApi.upload(this.avatarTempPath)
            await userApi.updateProfile({ avatarUrl: imageUrl })
            userManager.saveUserInfo({
              ...userManager.getUserInfo(),
              avatarUrl: imageUrl
            })
          } catch (e) {
            console.error('头像上传失败:', e)
          }
        }

        this.readyToEnter = true
        this.needProfile = false
        this.nickname = nickname
        uni.showToast({ title: '资料已保存', icon: 'success' })
      } catch (error) {
        console.error('完善资料失败:', error)
        uni.showToast({ title: '保存失败，请重试', icon: 'none' })
      } finally {
        this.loading = false
      }
    },

    goToIndex() {
      uni.switchTab({
        url: '/pages/index/index',
        fail: () => {
          uni.reLaunch({ url: '/pages/index/index' })
        }
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F7F3EE;
}

.hero {
  padding-right: 48rpx;
  padding-bottom: 88rpx;
  padding-left: 48rpx;
  background: linear-gradient(165deg, #C56A42 0%, #B85C38 48%, #8F4528 100%);
  border-radius: 0 0 40rpx 40rpx;
}

.brand-name {
  display: block;
  font-size: 56rpx;
  font-weight: 700;
  color: #FFFBF7;
  letter-spacing: 4rpx;
  line-height: 1.25;
}

.brand-line {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: rgba(255, 251, 247, 0.82);
  letter-spacing: 2rpx;
}

.login-panel {
  position: relative;
  z-index: 1;
  margin: -48rpx 40rpx 0;
  background: #FFFBF7;
  border-radius: 24rpx;
  padding: 40rpx 36rpx 36rpx;
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
  margin-bottom: 36rpx;
}

.profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 36rpx;
}

.avatar-btn {
  position: relative;
  width: 144rpx;
  height: 144rpx;
  padding: 0;
  margin: 0;
  background: transparent;
  border: none;
  border-radius: 50%;
  line-height: 0;
  overflow: visible;
}

.avatar-btn::after {
  display: none;
}

.avatar {
  width: 144rpx;
  height: 144rpx;
  border-radius: 50%;
  border: 4rpx solid #E8E0D6;
  background: #F7F3EE;
}

.avatar-badge {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: #B85C38;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid #FFFBF7;
}

.nickname {
  margin-top: 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #2A2420;
  line-height: 1.3;
}

.profile-hint {
  margin: 16rpx 0 24rpx;
  font-size: 22rpx;
  color: #9A9086;
}

.nickname-input {
  text-align: center;
}

.login-btn {
  height: 92rpx;
  font-size: 30rpx;
}

.footer-tip {
  margin-top: auto;
  padding: 48rpx 24rpx calc(32rpx + env(safe-area-inset-bottom));
  text-align: center;
  font-size: 22rpx;
  color: #9A9086;
}
</style>
