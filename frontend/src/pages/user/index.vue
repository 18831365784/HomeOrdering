<template>
  <view class="page">
    <view class="identity">
      <view class="identity-main">
        <view class="avatar-wrap" @click="showAvatarPicker">
          <image
            class="avatar"
            :src="userInfo.avatarUrl || '/static/icons/default-avatar.png'"
            mode="aspectFill"
          />
          <view class="avatar-badge">
            <AppIcon name="camera" :size="22" color="#FFFFFF" />
          </view>
        </view>
        <view class="identity-text">
          <view class="name-row" @click="showNicknameEditor">
            <text class="nickname">{{ userInfo.nickname || '设置昵称' }}</text>
            <AppIcon name="edit" :size="26" color="#9A9086" />
          </view>
          <text class="role">{{ isAdmin ? '家庭管理员' : '家庭成员' }}</text>
        </view>
      </view>
      <view class="balance-box">
        <view>
          <text class="balance-label">余额</text>
          <text class="balance-value">¥{{ (userInfo.balance || 0).toFixed(2) }}</text>
        </view>
        <button v-if="isAdmin" class="btn btn-sm btn-outline" @click="showBalanceEditor">设置</button>
      </view>
    </view>

    <view v-if="familyInfo" class="block">
      <view class="family-row" @click="goFamilyManage">
        <view class="family-left">
          <view class="family-icon">
            <AppIcon name="family" :size="34" color="#B85C38" />
          </view>
          <view>
            <text class="family-name">{{ familyInfo.name }}</text>
            <text class="invite">邀请码 {{ familyInfo.inviteCode }}</text>
          </view>
        </view>
        <AppIcon name="chevron" :size="24" color="#9A9086" />
      </view>
    </view>

    <view v-if="isAdmin" class="block">
      <text class="section-label">管理</text>
      <MenuRow icon="dish" title="菜品管理" @click="goDishManage" />
      <MenuRow icon="tag" title="分类管理" @click="goCategoryManage" />
    </view>

    <view class="block">
      <text class="section-label">订单</text>
      <MenuRow icon="order" title="历史订单" @click="goOrderList" />
    </view>

    <view class="logout-wrap">
      <button class="btn btn-ghost btn-block" @click="handleLogout">退出登录</button>
    </view>

    <AppModal
      :show="showAvatarModal"
      title="更换头像"
      :show-footer="false"
      @update:show="showAvatarModal = $event"
    >
      <view class="avatar-opts">
        <view class="avatar-opt" @click="useWechatAvatar">
          <AppIcon name="family" :size="36" color="#B85C38" />
          <text>使用微信头像</text>
        </view>
        <view class="avatar-opt" @click="chooseImage">
          <AppIcon name="camera" :size="36" color="#B85C38" />
          <text>从相册选择</text>
        </view>
      </view>
    </AppModal>

    <AppModal
      :show="showNicknameModal"
      title="修改昵称"
      confirm-text="保存"
      @update:show="showNicknameModal = $event"
      @confirm="saveNickname"
    >
      <input class="input" v-model="tempNickname" placeholder="请输入昵称" maxlength="20" />
    </AppModal>

    <AppModal
      :show="showBalanceModal"
      title="设置余额"
      confirm-text="保存"
      @update:show="showBalanceModal = $event"
      @confirm="saveBalance"
    >
      <input class="input" type="digit" v-model="tempBalance" placeholder="请输入余额" />
    </AppModal>
  </view>
</template>

<script>
import { userApi, familyApi, fileApi, authApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'
import AppIcon from '@/components/AppIcon.vue'
import MenuRow from '@/components/MenuRow.vue'
import AppModal from '@/components/AppModal.vue'

export default {
  components: { AppIcon, MenuRow, AppModal },
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
    this.loadUserInfo()
    this.loadFamilyInfo()
  },

  methods: {
    async checkFamilyAndRedirect() {
      try {
        const uuid = userManager.getUuid()
        if (!uuid) {
          uni.reLaunch({ url: '/pages/login/index' })
          return
        }
        const info = await familyApi.getFamilyInfo()
        if (!info) {
          uni.redirectTo({ url: '/pages/family/index' })
        } else {
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
        uni.redirectTo({ url: '/pages/family/index' })
      }
    },

    async loadUserInfo() {
      try {
        const uuid = userManager.getUuid()
        if (!uuid) return
        const userData = await userApi.getInfo()
        this.userInfo = userData
        this.isAdmin = !!userData.isAdmin
        userManager.saveUserInfo({
          ...userManager.getUserInfo(),
          ...userData,
          isAdmin: !!userData.isAdmin
        })
      } catch (e) {
        console.error('加载用户信息失败', e)
      }
    },

    async loadFamilyInfo() {
      try {
        const uuid = userManager.getUuid()
        if (!uuid) return
        const info = await familyApi.getFamilyInfo()
        if (info) {
          this.familyInfo = info
          this.isAdmin = info.isAdmin || false
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

    goFamilyManage() {
      uni.navigateTo({ url: '/pages/family/index' })
    },

    showAvatarPicker() {
      this.showAvatarModal = true
    },

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

    chooseImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album'],
        success: async (res) => {
          const tempPath = res.tempFilePaths[0]
          try {
            uni.showLoading({ title: '上传中...' })
            const imageUrl = await fileApi.upload(tempPath)
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

    async updateAvatar(avatarUrl) {
      try {
        const uuid = userManager.getUuid()
        await userApi.updateProfile({ uuid, avatarUrl })
        this.userInfo.avatarUrl = avatarUrl
        userManager.saveUserInfo({
          ...userManager.getUserInfo(),
          avatarUrl
        })
        uni.showToast({ title: '头像更新成功', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '更新失败', icon: 'none' })
      }
    },

    showNicknameEditor() {
      this.tempNickname = this.userInfo.nickname || ''
      this.showNicknameModal = true
    },

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

    showBalanceEditor() {
      this.tempBalance = (this.userInfo.balance || 0).toString()
      this.showBalanceModal = true
    },

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

    goDishManage() {
      uni.navigateTo({ url: '/pages/dish/manage' })
    },

    goCategoryManage() {
      uni.navigateTo({ url: '/pages/category/manage' })
    },

    goOrderList() {
      uni.switchTab({ url: '/pages/order/list' })
    },

    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await authApi.logout()
            } catch (e) {}
            userManager.clearUserInfo()
            uni.reLaunch({ url: '/pages/login/index' })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100%;
  background: #F7F3EE;
  padding: 24rpx;
  padding-bottom: calc(48rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.identity {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
}

.identity-main {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 28rpx;
}

.avatar-wrap {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  flex-shrink: 0;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
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
  border: 3rpx solid #FFFFFF;
}

.identity-text {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.nickname {
  font-size: 34rpx;
  font-weight: 700;
  color: #2A2420;
}

.role {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #9A9086;
}

.balance-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  background: #F7F3EE;
  border-radius: 16rpx;
}

.balance-label {
  display: block;
  font-size: 22rpx;
  color: #9A9086;
  margin-bottom: 4rpx;
}

.balance-value {
  font-size: 40rpx;
  font-weight: 700;
  color: #B85C38;
}

.block {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 12rpx 24rpx 16rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
}

.section-label {
  display: block;
  font-size: 22rpx;
  font-weight: 600;
  color: #9A9086;
  letter-spacing: 2rpx;
  padding: 16rpx 8rpx 12rpx;
}

.family-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 8rpx;
}

.family-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.family-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  background: rgba(184, 92, 56, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
}

.family-name {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #2A2420;
}

.invite {
  display: block;
  margin-top: 4rpx;
  font-size: 24rpx;
  color: #9A9086;
}

.logout-wrap {
  margin-top: 24rpx;
  padding: 0 8rpx;
}

.avatar-opts {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.avatar-opt {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx 24rpx;
  background: #F7F3EE;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #2A2420;
}
</style>
