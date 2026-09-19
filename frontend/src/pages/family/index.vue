<template>
  <view class="page">
    <view v-if="hasFamily" class="content">
      <view class="hero">
        <text class="hero-name">{{ familyInfo.name }}</text>
        <view class="code-box">
          <text class="code-label">邀请码</text>
          <text class="code-value">{{ familyInfo.inviteCode }}</text>
        </view>
        <view v-if="isAdmin" class="hero-actions">
          <button class="btn btn-sm btn-outline" @tap.stop="onEditName">修改名称</button>
          <button class="btn btn-sm btn-ghost" @tap.stop="regenerateCode">换邀请码</button>
        </view>
      </view>

      <view class="section">
        <text class="section-label">家庭成员</text>
        <view
          v-for="member in members"
          :key="member.uuid"
          class="member"
          @click="onMemberClick(member)"
        >
          <image
            class="member-avatar"
            :src="member.avatarUrl || '/static/icons/default-avatar.png'"
            mode="aspectFill"
          />
          <view class="member-info">
            <text class="member-name">{{ member.nickname || '用户' }}</text>
            <text v-if="member.isAdmin" class="member-tag">管理员</text>
          </view>
          <view class="member-right">
            <text class="balance">¥{{ (member.balance || 0).toFixed(2) }}</text>
            <AppIcon v-if="isAdmin" name="chevron" :size="22" color="#9A9086" />
          </view>
        </view>
      </view>
    </view>

    <view v-else class="empty-wrap">
      <EmptyState
        icon="family"
        title="还没有家庭"
        text="创建新家庭，或用邀请码加入"
      />
      <view class="empty-actions">
        <button class="btn btn-primary btn-block" @click="showCreateModal = true">创建新家庭</button>
        <button class="btn btn-outline btn-block" @click="showJoinModal = true">输入邀请码加入</button>
      </view>
    </view>

    <AppModal
      :show="showCreateModal"
      title="创建家庭"
      confirm-text="创建"
      @update:show="showCreateModal = $event"
      @confirm="handleCreate"
    >
      <input class="input" v-model="createName" placeholder="给家庭取个名字" maxlength="20" />
    </AppModal>

    <AppModal
      :show="showJoinModal"
      title="加入家庭"
      confirm-text="加入"
      @update:show="showJoinModal = $event"
      @confirm="handleJoin"
    >
      <input class="input" v-model="joinCode" placeholder="请输入6位邀请码" maxlength="6" />
    </AppModal>

    <AppModal
      :show="showRenameModal"
      title="修改家庭名称"
      confirm-text="保存"
      @update:show="showRenameModal = $event"
      @confirm="handleRename"
    >
      <input class="input" v-model="renameName" placeholder="请输入新的家庭名称" maxlength="20" />
    </AppModal>

    <AppModal
      :show="showBalanceModal"
      :title="'给 ' + (selectedMember && selectedMember.nickname ? selectedMember.nickname : '成员') + ' 充值'"
      confirm-text="充值"
      @update:show="showBalanceModal = $event"
      @confirm="handleAddBalance"
    >
      <input class="input" type="digit" v-model="tempBalance" placeholder="请输入充值金额" />
    </AppModal>
  </view>
</template>

<script>
import { familyApi, userApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'
import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import AppModal from '@/components/AppModal.vue'

export default {
  components: { AppIcon, EmptyState, AppModal },
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
        }, 1200)
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
        const currentBalance = this.selectedMember.balance || 0
        const newBalance = currentBalance + amount
        await userApi.updateBalance(this.selectedMember.uuid, newBalance)
        uni.hideLoading()
        this.showBalanceModal = false
        this.tempBalance = ''
        this.selectedMember = null
        uni.showToast({ title: '充值成功', icon: 'success' })
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
.page {
  min-height: 100%;
  background: #F7F3EE;
  box-sizing: border-box;
}

.content {
  padding: 16rpx 24rpx 40rpx;
  box-sizing: border-box;
}

.hero {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 40rpx 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
}

.hero-name {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #2A2420;
  margin-bottom: 24rpx;
}

.code-box {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  background: #F7F3EE;
  border-radius: 14rpx;
  margin-bottom: 24rpx;
}

.code-label {
  font-size: 24rpx;
  color: #9A9086;
}

.code-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #B85C38;
  letter-spacing: 4rpx;
}

.hero-actions {
  display: flex;
  gap: 16rpx;
}

.section {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 8rpx 24rpx 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
}

.section-label {
  display: block;
  font-size: 22rpx;
  font-weight: 600;
  color: #9A9086;
  letter-spacing: 2rpx;
  padding: 20rpx 8rpx 8rpx;
}

.member {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 8rpx;
  border-bottom: 1rpx solid #E8E0D6;
}

.member:last-child {
  border-bottom: none;
}

.member-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #F7F3EE;
}

.member-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  min-width: 0;
}

.member-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #2A2420;
}

.member-tag {
  font-size: 20rpx;
  color: #5C6B5A;
  background: rgba(92, 107, 90, 0.12);
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
}

.member-right {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.balance {
  font-size: 28rpx;
  font-weight: 600;
  color: #B85C38;
}

.empty-wrap {
  padding: 48rpx 40rpx;
}

.empty-actions {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-top: 8rpx;
}
</style>
