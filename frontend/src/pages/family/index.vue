<template>
  <view class="container">
    <view class="header">
      <view class="header-bg"></view>
      <view class="header-content">
        <text class="title">🏠 家庭</text>
        <text class="subtitle">{{ hasFamily ? '管理您的家庭' : '创建或加入一个家庭' }}</text>
      </view>
    </view>

    <!-- 已加入家庭 -->
    <view v-if="hasFamily" class="content">
      <view class="family-card card">
        <view class="family-info">
          <text class="family-name">{{ familyInfo.name }}</text>
          <view class="invite-code-box">
            <text class="invite-label">邀请码</text>
            <text class="invite-code">{{ familyInfo.inviteCode }}</text>
          </view>
        </view>
        <view class="family-actions">
          <button v-if="isAdmin" class="btn btn-outline" @tap.stop="onEditName">修改名称</button>
          <button v-if="isAdmin" class="btn btn-outline" @tap.stop="regenerateCode">换邀请码</button>
        </view>
      </view>

      <view class="section">
        <text class="section-title">家庭成员</text>
        <view class="member-list">
          <view v-for="member in members" :key="member.uuid" class="member-item" @click="onMemberClick(member)">
            <image class="member-avatar" :src="member.avatarUrl || '/static/icons/home.png'" mode="aspectFill" />
            <view class="member-info">
              <text class="member-name">{{ member.nickname || '用户' }}</text>
              <text v-if="member.isAdmin" class="member-tag">管理员</text>
            </view>
            <view class="member-balance">
              <text class="balance-text">¥{{ (member.balance || 0).toFixed(2) }}</text>
              <text v-if="isAdmin" class="balance-arrow">›</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 未加入家庭 -->
    <view v-else class="content">
      <view class="empty-family">
        <text class="empty-icon">🏠</text>
        <text class="empty-text">还没有加入家庭</text>
      </view>

      <view class="action-buttons">
        <button class="btn btn-coral" @click="showCreateModal = true">创建新家庭</button>
        <button class="btn btn-outline" @click="showJoinModal = true">输入邀请码加入</button>
      </view>
    </view>

    <!-- 创建家庭弹窗 -->
    <view v-if="showCreateModal" class="modal-mask" @click="showCreateModal = false">
      <view class="modal-card" @click.stop>
        <view class="modal-title">创建家庭</view>
        <view class="modal-body">
          <input
            class="input"
            v-model="createName"
            placeholder="给家庭取个名字"
            maxlength="20"
          />
        </view>
        <view class="modal-actions">
          <button class="btn btn-ghost" @click="showCreateModal = false">取消</button>
          <button class="btn btn-coral" @click="handleCreate">创建</button>
        </view>
      </view>
    </view>

    <!-- 加入家庭弹窗 -->
    <view v-if="showJoinModal" class="modal-mask" @click="showJoinModal = false">
      <view class="modal-card" @click.stop>
        <view class="modal-title">加入家庭</view>
        <view class="modal-body">
          <input
            class="input"
            v-model="joinCode"
            placeholder="请输入6位邀请码"
            maxlength="6"
          />
        </view>
        <view class="modal-actions">
          <button class="btn btn-ghost" @click="showJoinModal = false">取消</button>
          <button class="btn btn-coral" @click="handleJoin">加入</button>
        </view>
      </view>
    </view>

    <!-- 修改家庭名称弹窗 -->
    <view v-if="showRenameModal" class="modal-mask" @click="showRenameModal = false">
      <view class="modal-card" @click.stop>
        <view class="modal-title">修改家庭名称</view>
        <view class="modal-body">
          <input
            class="input"
            v-model="renameName"
            placeholder="请输入新的家庭名称"
            maxlength="20"
          />
        </view>
        <view class="modal-actions">
          <button class="btn btn-ghost" @tap.stop="showRenameModal = false">取消</button>
          <button class="btn btn-coral" @tap.stop="handleRename">保存</button>
        </view>
      </view>
    </view>

    <!-- 余额充值弹窗 -->
    <view v-if="showBalanceModal" class="modal-mask" @click="showBalanceModal = false">
      <view class="modal-card" @click.stop>
        <view class="modal-title">给 {{ selectedMember?.nickname || '成员' }} 充值</view>
        <view class="modal-body">
          <input
            class="input balance-input"
            type="digit"
            v-model="tempBalance"
            placeholder="请输入充值金额"
          />
        </view>
        <view class="modal-actions">
          <button class="btn btn-ghost" @click="showBalanceModal = false">取消</button>
          <button class="btn btn-mint" @click="handleAddBalance">充值</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { familyApi, userApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'

export default {
  data() {
    return {
      hasFamily: false,
      familyInfo: {},
      members: [],
      isAdmin: false,
      showCreateModal: false,
      showJoinModal: false,
      showBalanceModal: false,
      showRenameModal: false,
      createName: '',
      joinCode: '',
      renameName: '',
      selectedMember: null,
      tempBalance: ''
    }
  },

  onLoad() {
    this.loadFamilyInfo()
  },

  onShow() {
    this.loadFamilyInfo()
  },

  methods: {
    async loadFamilyInfo() {
      try {
        const uuid = userManager.getUuid()
        if (!uuid) return

        const info = await familyApi.getFamilyInfo()
        if (info) {
          this.hasFamily = true
          this.familyInfo = {
            name: info.name,
            inviteCode: info.inviteCode
          }
          this.members = info.members || []
          this.isAdmin = info.isAdmin || false
        } else {
          this.hasFamily = false
        }
      } catch (e) {
        console.error('获取家庭信息失败', e)
        this.hasFamily = false
      }
    },

    async handleCreate() {
      const name = (this.createName || '').trim()
      if (!name) {
        uni.showToast({ title: '请输入家庭名称', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '创建中...' })
        const family = await familyApi.createFamily(name)
        uni.hideLoading()
        this.showCreateModal = false
        this.createName = ''
        this.hasFamily = true
        this.familyInfo = {
          name: family.name,
          inviteCode: family.inviteCode
        }
        this.members = family.members || []
        this.isAdmin = !!family.isAdmin
        userManager.saveUserInfo({
          ...userManager.getUserInfo(),
          familyId: family.id,
          familyName: family.name,
          isAdmin: family.isAdmin
        })
        uni.showToast({ title: '创建成功', icon: 'success' })
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: (e && e.message) || '创建失败', icon: 'none' })
      }
    },

    onEditName() {
      if (!this.isAdmin) {
        uni.showToast({ title: '仅管理员可修改', icon: 'none' })
        return
      }
      this.renameName = this.familyInfo.name || ''
      this.showRenameModal = true
    },

    async handleRename() {
      const name = (this.renameName || '').trim()
      if (!name) {
        uni.showToast({ title: '请输入家庭名称', icon: 'none' })
        return
      }
      try {
        uni.showLoading({ title: '保存中...' })
        const family = await familyApi.updateFamilyName(name)
        uni.hideLoading()
        this.showRenameModal = false
        this.familyInfo.name = family.name
        userManager.saveUserInfo({
          ...userManager.getUserInfo(),
          familyName: family.name
        })
        uni.showToast({ title: '已修改', icon: 'success' })
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: (e && e.message) || '修改失败', icon: 'none' })
      }
    },

    async handleJoin() {
      if (!this.joinCode.trim() || this.joinCode.length !== 6) {
        uni.showToast({ title: '请输入6位邀请码', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '加入中...' })
        await familyApi.joinFamily(this.joinCode.trim().toUpperCase())
        uni.hideLoading()
        this.showJoinModal = false
        this.joinCode = ''
        uni.showToast({ title: '加入成功', icon: 'success' })

        setTimeout(() => {
          uni.switchTab({ url: '/pages/user/index' })
        }, 1500)
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: e.message || '加入失败', icon: 'none' })
      }
    },

    async regenerateCode() {
      try {
        uni.showLoading({ title: '生成中...' })
        const newCode = await familyApi.regenerateInviteCode()
        this.familyInfo.inviteCode = newCode
        uni.hideLoading()
        uni.showToast({ title: '已生成新邀请码', icon: 'success' })
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '生成失败', icon: 'none' })
      }
    },

    onMemberClick(member) {
      if (!this.isAdmin) return
      this.selectedMember = member
      this.tempBalance = ''
      this.showBalanceModal = true
    },

    async handleAddBalance() {
      const amount = parseFloat(this.tempBalance)
      if (isNaN(amount) || amount <= 0) {
        uni.showToast({ title: '请输入有效金额', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '充值中...' })
        // 获取当前余额 + 充值金额
        const currentBalance = this.selectedMember.balance || 0
        const newBalance = currentBalance + amount
        await userApi.updateBalance(this.selectedMember.uuid, newBalance)
        uni.hideLoading()
        this.showBalanceModal = false
        this.tempBalance = ''
        this.selectedMember = null
        uni.showToast({ title: '充值成功', icon: 'success' })
        // 刷新家庭成员信息
        this.loadFamilyInfo()
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '充值失败', icon: 'none' })
      }
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

.header {
  position: relative;
  padding-top: 60rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 280rpx;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  border-radius: 0 0 60rpx 60rpx;
}

.header-content {
  position: relative;
  padding: 60rpx 32rpx 40rpx;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #FFFFFF;
  display: block;
}

.subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
  display: block;
  margin-top: 12rpx;
}

.content {
  padding: 24rpx 32rpx;
}

.family-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.family-info {
  flex: 1;
}

.family-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #212121;
  display: block;
  margin-bottom: 16rpx;
}

.family-actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-left: 16rpx;
}

.family-actions .btn {
  padding: 10rpx 24rpx;
  font-size: 24rpx;
  border-radius: 30rpx;
  margin: 0;
  line-height: 1.4;
}

.invite-code-box {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 16rpx;
}

.invite-label {
  font-size: 24rpx;
  color: #9E9E9E;
}

.invite-code {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B6B;
  letter-spacing: 4rpx;
}

.section {
  margin-top: 32rpx;
}

.section-title {
  font-size: 26rpx;
  color: #9E9E9E;
  margin-bottom: 16rpx;
  display: block;
}

.member-list {
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx;
  border-bottom: 1rpx solid #F5F5F5;
}

.member-item:last-child {
  border-bottom: none;
}

.member-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.member-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.member-name {
  font-size: 30rpx;
  color: #212121;
}

.member-tag {
  font-size: 22rpx;
  color: #4ECDC4;
  background: rgba(78, 205, 196, 0.15);
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.member-balance {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.balance-text {
  font-size: 28rpx;
  color: #FF6B6B;
  font-weight: 600;
}

.balance-arrow {
  font-size: 28rpx;
  color: #BDBDBD;
}

.empty-family {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
}

.empty-icon {
  font-size: 160rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #9E9E9E;
  margin-top: 24rpx;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding: 0 32rpx;
}

.action-buttons .btn {
  width: 100%;
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

.balance-input {
  text-align: center;
  font-size: 36rpx;
}
</style>