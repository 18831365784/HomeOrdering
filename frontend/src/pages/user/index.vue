<template>
  <view class="container">
    <!-- 顶部用户卡片 -->
    <view class="user-header">
      <view class="header-bg"></view>
      <view class="user-info">
        <!-- 头像 -->
        <view class="avatar-wrapper" @click="showAvatarPicker">
          <image
            class="avatar"
            :src="userInfo.avatarUrl || '/static/icons/home.png'"
            mode="aspectFill"
          />
          <view class="avatar-edit">
            <text class="edit-icon">📷</text>
          </view>
        </view>

        <!-- 用户名 -->
        <view class="nickname-row" @click="showNicknameEditor">
          <text class="nickname">{{ userInfo.nickname || '设置昵称' }}</text>
          <text class="edit-hint">✏️</text>
        </view>

        <!-- 余额显示 -->
        <view class="balance-card">
          <view class="balance-info">
            <text class="balance-label">我的余额</text>
            <text class="balance-value">¥{{ (userInfo.balance || 0).toFixed(2) }}</text>
          </view>
          <view v-if="isAdmin" class="balance-action" @click="showBalanceEditor">
            <text class="action-text">设置</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 家庭信息卡片 -->
    <view class="family-section">
      <view v-if="familyInfo" class="family-card card">
        <view class="family-header">
          <text class="family-icon">🏠</text>
          <view class="family-info">
            <text class="family-name">{{ familyInfo.name }}</text>
            <text class="family-role">{{ isAdmin ? '家庭管理员' : '家庭成员' }}</text>
          </view>
          <view v-if="isAdmin" class="family-action" @click="goFamilyManage">
            <text class="action-text">管理</text>
            <text class="arrow">›</text>
          </view>
        </view>
        <view class="invite-code-row">
          <text class="invite-label">邀请码：</text>
          <text class="invite-code">{{ familyInfo.inviteCode }}</text>
        </view>
      </view>
    </view>

    <!-- 管理功能（管理员可见） -->
    <view v-if="isAdmin" class="section">
      <view class="section-title">管理功能</view>
      <view class="menu-list">
        <view class="menu-item" @click="goDishManage">
          <view class="menu-left">
            <text class="menu-icon">🍽️</text>
            <text class="menu-text">菜品管理</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goCategoryManage">
          <view class="menu-left">
            <text class="menu-icon">🏷️</text>
            <text class="menu-text">分类管理</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 通用功能 -->
    <view class="section">
      <view class="section-title">我的订单</view>
      <view class="menu-list">
        <view class="menu-item" @click="goOrderList">
          <view class="menu-left">
            <text class="menu-icon">📜</text>
            <text class="menu-text">历史订单</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section">
      <button class="btn btn-outline logout-btn" @click="handleLogout">退出登录</button>
    </view>

    <!-- 头像选择弹窗 -->
    <view v-if="showAvatarModal" class="modal-mask" @click="showAvatarModal = false">
      <view class="modal-card" @click.stop>
        <view class="modal-title">更换头像</view>
        <view class="modal-body">
          <view class="avatar-option" @click="useWechatAvatar">
            <text class="option-icon">💬</text>
            <text class="option-text">使用微信头像</text>
          </view>
          <view class="avatar-option" @click="chooseImage">
            <text class="option-icon">📷</text>
            <text class="option-text">从相册选择</text>
          </view>
        </view>
        <view class="modal-cancel" @click="showAvatarModal = false">取消</view>
      </view>
    </view>

    <!-- 昵称编辑弹窗 -->
    <view v-if="showNicknameModal" class="modal-mask" @click="showNicknameModal = false">
      <view class="modal-card" @click.stop>
        <view class="modal-title">修改昵称</view>
        <view class="modal-body">
          <input
            class="input nickname-input"
            v-model="tempNickname"
            placeholder="请输入昵称"
            maxlength="20"
          />
        </view>
        <view class="modal-actions">
          <button class="btn btn-ghost" @click="showNicknameModal = false">取消</button>
          <button class="btn btn-coral" @click="saveNickname">保存</button>
        </view>
      </view>
    </view>

    <!-- 余额编辑弹窗（管理员） -->
    <view v-if="showBalanceModal" class="modal-mask" @click="showBalanceModal = false">
      <view class="modal-card" @click.stop>
        <view class="modal-title">设置余额</view>
        <view class="modal-body">
          <input
            class="input nickname-input"
            type="digit"
            v-model="tempBalance"
            placeholder="请输入余额"
          />
        </view>
        <view class="modal-actions">
          <button class="btn btn-ghost" @click="showBalanceModal = false">取消</button>
          <button class="btn btn-mint" @click="saveBalance">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { userApi } from '@/utils/api.js'
import familyApi from '@/utils/familyApi.js'
import userManager from '@/utils/user.js'

export default {
  data() {
    return {
      userInfo: {},
      familyInfo: null,
      isAdmin: false,
      showAvatarModal: false,
      showNicknameModal: false,
      showBalanceModal: false,
      tempNickname: '',
      tempBalance: ''
    }
  },

  onLoad() {
    this.checkFamilyAndRedirect()
  },

  onShow() {
    // 每次显示都检查家庭状态并刷新数据
    this.loadUserInfo()
    this.loadFamilyInfo()
  },

  methods: {
    // 检查用户是否有家庭，没有则跳转到家庭页面
    async checkFamilyAndRedirect() {
      try {
        const uuid = userManager.getUuid()
        if (!uuid) {
          uni.reLaunch({ url: '/pages/login/index' })
          return
        }

        const info = await familyApi.getFamilyInfo(uuid)
        if (!info) {
          // 用户没有家庭，跳转到家庭页
          uni.redirectTo({ url: '/pages/family/index' })
        } else {
          // 有家庭，更新本地存储并加载数据
          this.familyInfo = info
          this.isAdmin = info.isAdmin || false
          userManager.saveUserInfo({
            ...userManager.getUserInfo(),
            familyId: info.id,
            familyName: info.name,
            inviteCode: info.inviteCode,
            isAdmin: info.isAdmin
          })
          this.loadUserInfo()
        }
      } catch (e) {
        console.error('检查家庭状态失败', e)
        // 出错也跳转到家庭页
        uni.redirectTo({ url: '/pages/family/index' })
      }
    },

    // 加载用户信息
    async loadUserInfo() {
      try {
        const uuid = userManager.getUuid()
        if (!uuid) return

        const userData = await userApi.getInfo(uuid)
        this.userInfo = userData
        this.isAdmin = userData.role === 1 || false

        // 更新本地存储
        userManager.saveUserInfo({
          ...userManager.getUserInfo(),
          ...userData
        })
      } catch (e) {
        console.error('加载用户信息失败', e)
      }
    },

    // 加载家庭信息
    async loadFamilyInfo() {
      try {
        const uuid = userManager.getUuid()
        if (!uuid) return

        const info = await familyApi.getFamilyInfo(uuid)
        if (info) {
          this.familyInfo = info
          this.isAdmin = info.isAdmin || false

          // 更新本地存储的家庭信息
          userManager.saveUserInfo({
            ...userManager.getUserInfo(),
            familyId: info.id,
            familyName: info.name,
            inviteCode: info.inviteCode,
            isAdmin: info.isAdmin
          })
        }
      } catch (e) {
        console.error('加载家庭信息失败', e)
      }
    },

    // 跳转家庭管理页面
    goFamilyManage() {
      uni.navigateTo({ url: '/pages/family/index' })
    },

    // 显示头像选择
    showAvatarPicker() {
      this.showAvatarModal = true
    },

    // 使用微信头像
    useWechatAvatar() {
      uni.getUserProfile({
        desc: '获取您的头像',
        success: (res) => {
          if (res.userInfo && res.userInfo.avatarUrl) {
            this.updateAvatar(res.userInfo.avatarUrl)
          }
        },
        fail: () => {
          uni.showToast({ title: '请允许获取头像权限', icon: 'none' })
        }
      })
      this.showAvatarModal = false
    },

    // 从相册选择
    chooseImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album'],
        success: async (res) => {
          const tempPath = res.tempFilePaths[0]
          try {
            uni.showLoading({ title: '上传中...' })
            const imageUrl = await new Promise((resolve, reject) => {
              uni.uploadFile({
                url: 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/file/upload',
                filePath: tempPath,
                name: 'file',
                header: { 'ngrok-skip-browser-warning': 'true' },
                success: (res) => {
                  const data = JSON.parse(res.data)
                  if (data.code === 200) {
                    resolve(data.data)
                  } else {
                    reject(new Error(data.message))
                  }
                },
                fail: reject
              })
            })
            this.updateAvatar(imageUrl)
          } catch (e) {
            uni.showToast({ title: '上传失败', icon: 'none' })
          } finally {
            uni.hideLoading()
          }
        }
      })
      this.showAvatarModal = false
    },

    // 更新头像
    async updateAvatar(avatarUrl) {
      try {
        const uuid = userManager.getUuid()
        await userApi.updateProfile({ uuid, avatarUrl })
        this.userInfo.avatarUrl = avatarUrl
        // 更新本地存储
        userManager.saveUserInfo({
          ...userManager.getUserInfo(),
          avatarUrl: avatarUrl
        })
        uni.showToast({ title: '头像更新成功', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '更新失败', icon: 'none' })
      }
    },

    // 显示昵称编辑器
    showNicknameEditor() {
      this.tempNickname = this.userInfo.nickname || ''
      this.showNicknameModal = true
    },

    // 保存昵称
    async saveNickname() {
      if (!this.tempNickname.trim()) {
        uni.showToast({ title: '昵称不能为空', icon: 'none' })
        return
      }
      try {
        const uuid = userManager.getUuid()
        await userApi.updateProfile({ uuid, nickname: this.tempNickname.trim() })
        this.userInfo.nickname = this.tempNickname.trim()
        this.showNicknameModal = false
        uni.showToast({ title: '昵称更新成功', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '更新失败', icon: 'none' })
      }
    },

    // 显示余额编辑器
    showBalanceEditor() {
      this.tempBalance = (this.userInfo.balance || 0).toString()
      this.showBalanceModal = true
    },

    // 保存余额
    async saveBalance() {
      const balance = parseFloat(this.tempBalance)
      if (isNaN(balance) || balance < 0) {
        uni.showToast({ title: '请输入有效金额', icon: 'none' })
        return
      }
      try {
        const uuid = userManager.getUuid()
        await userApi.updateBalance(uuid, balance)
        this.userInfo.balance = balance
        this.showBalanceModal = false
        uni.showToast({ title: '余额更新成功', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '更新失败', icon: 'none' })
      }
    },

    // 跳转菜品管理
    goDishManage() {
      uni.navigateTo({ url: '/pages/dish/manage' })
    },

    // 跳转分类管理
    goCategoryManage() {
      uni.navigateTo({ url: '/pages/category/manage' })
    },

    // 跳转订单列表
    goOrderList() {
      uni.switchTab({ url: '/pages/order/list' })
    },

    // 退出登录
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            userManager.clearUserInfo()
            uni.reLaunch({
              url: '/pages/login/index'
            })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #FAFAFA;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* 顶部用户卡片 */
.user-header {
  position: relative;
  padding-top: 80rpx;
  padding-top: calc(80rpx + constant(safe-area-inset-top));
  padding-top: calc(80rpx + env(safe-area-inset-top));
  padding-bottom: 40rpx;
  overflow: visible;
  z-index: 1;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 380rpx;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  border-radius: 0 0 80rpx 80rpx;
  z-index: 0;
}

.user-info {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 32rpx;
}

/* 头像 */
.avatar-wrapper {
  position: relative;
  width: 160rpx;
  height: 160rpx;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  border: 6rpx solid #FFFFFF;
  box-shadow: 0 8rpx 32rpx rgba(255, 107, 107, 0.3);
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 56rpx;
  height: 56rpx;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid #FFFFFF;
}

.edit-icon {
  font-size: 28rpx;
}

/* 昵称 */
.nickname-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 24rpx;
}

.nickname {
  font-size: 36rpx;
  font-weight: 600;
  color: #FFFFFF;
}

.edit-hint {
  width: 48rpx;
  height: 48rpx;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
}

/* 余额卡片 */
.balance-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  max-width: 600rpx;
  margin-top: 24rpx;
  padding: 28rpx 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(255, 107, 107, 0.15);
}

/* 家庭区块 */
.family-section {
  padding: 0 32rpx;
  margin-top: 24rpx;
}

.balance-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.balance-label {
  font-size: 24rpx;
  color: #9E9E9E;
}

.balance-value {
  font-size: 48rpx;
  font-weight: bold;
  color: #FF6B6B;
}

.balance-action {
  padding: 12rpx 24rpx;
  background: linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%);
  border-radius: 50rpx;
}

.action-text {
  font-size: 24rpx;
  color: #FFFFFF;
  font-weight: 500;
}

/* 家庭卡片 */
.family-card {
  background: linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%);
  color: #FFFFFF;
  padding: 32rpx;
  border-radius: 24rpx;
}

.family-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.family-icon {
  font-size: 60rpx;
}

.family-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.family-name {
  font-size: 36rpx;
  font-weight: bold;
}

.family-role {
  font-size: 24rpx;
  opacity: 0.9;
}

.family-action {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: rgba(255, 255, 255, 0.3);
  padding: 12rpx 24rpx;
  border-radius: 50rpx;
}

.family-action .action-text {
  font-size: 24rpx;
}

.family-action .arrow {
  font-size: 28rpx;
}

.invite-code-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.3);
}

.invite-label {
  font-size: 24rpx;
  opacity: 0.9;
}

.invite-code {
  font-size: 32rpx;
  font-weight: bold;
  letter-spacing: 4rpx;
}

/* 菜单区块 */
.section {
  padding: 24rpx 32rpx;
}

.section-title {
  font-size: 26rpx;
  color: #9E9E9E;
  margin-bottom: 16rpx;
  text-transform: uppercase;
  letter-spacing: 2rpx;
}

.menu-list {
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  border-bottom: 1rpx solid #F5F5F5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.menu-icon {
  font-size: 40rpx;
}

.menu-text {
  font-size: 30rpx;
  color: #212121;
}

.menu-arrow {
  font-size: 40rpx;
  color: #BDBDBD;
}

/* 退出登录 */
.logout-section {
  padding: 48rpx 32rpx;
}

.logout-btn {
  width: 100%;
  border-color: #FF6B6B;
  color: #FF6B6B;
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-card {
  width: 600rpx;
  background: #FFFFFF;
  border-radius: 32rpx;
  padding: 48rpx;
}

.modal-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #212121;
  text-align: center;
  margin-bottom: 32rpx;
}

.modal-body {
  margin-bottom: 32rpx;
}

.modal-actions {
  display: flex;
  gap: 20rpx;
}

.modal-actions .btn {
  flex: 1;
  padding: 10rpx 60rpx;
  font-size: 24rpx;
  border-radius: 30rpx;
}

.modal-cancel {
  text-align: center;
  font-size: 30rpx;
  color: #9E9E9E;
  padding-top: 24rpx;
  border-top: 1rpx solid #F5F5F5;
}

/* 头像选项 */
.avatar-option {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 32rpx;
  border-bottom: 1rpx solid #F5F5F5;
}

.avatar-option:last-child {
  border-bottom: none;
}

.option-icon {
  font-size: 48rpx;
}

.option-text {
  font-size: 30rpx;
  color: #212121;
}

/* 输入框 */
.nickname-input {
  text-align: center;
}
</style>