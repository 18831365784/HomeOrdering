<template>
  <view class="container">
    <view class="header">
      <text class="title">分类管理</text>
    </view>

    <view class="category-list">
      <view class="category-item card" v-for="c in categories" :key="c.id">
        <image v-if="c.iconUrl" class="icon" :src="c.iconUrl" mode="aspectFit" />
        <view v-else class="icon placeholder-image">无</view>
        <view class="info">
          <text class="name">{{ c.name }}</text>
          <text class="status" :class="{ on: c.status === 1 }">{{ c.status === 1 ? '上架' : '下架' }}</text>
        </view>
        <view class="actions">
          <button class="btn btn-secondary" @click="toggle(c)">{{ c.status === 1 ? '下架' : '上架' }}</button>
          <button class="btn btn-secondary" @click="edit(c)">编辑</button>
        </view>
      </view>
    </view>

    <!-- 编辑/新增弹窗 -->
    <view v-if="showModal" class="modal-mask" @touchmove.stop>
      <view class="modal-card card">
        <view class="modal-title">{{ form.id ? '编辑分类' : '新增分类' }}</view>
        <view class="modal-body">
          <input class="modal-input" placeholder="分类名称" v-model="form.name" />
          <view class="upload-row">
            <button class="btn btn-secondary" @click="chooseIcon">选择图标</button>
            <text class="text-muted" v-if="!form.iconUrl">未选择</text>
            <image v-else class="icon-preview" :src="form.iconUrl" mode="aspectFit" />
          </view>
        </view>
        <view class="modal-actions">
          <button class="btn btn-secondary" @click="close">取消</button>
          <button class="btn btn-primary primary-bg" @click="save">保存</button>
        </view>
      </view>
    </view>

    <!-- 右下角浮动新增按钮 -->
    <view class="fab-button primary-bg" @click="openCreate">
      <text class="fab-icon">新增</text>
    </view>
  </view>
  
</template>

<script>
import { categoryApi, fileApi } from '@/utils/api.js'

export default {
  data() {
    return {
      categories: [],
      showModal: false,
      form: { id: null, name: '', iconUrl: '', status: 1, sort: 0 }
    }
  },
  onShow() { this.load() },
  methods: {
    async load() {
      try {
        this.categories = await categoryApi.getList(null)
      } catch (e) { console.error(e) }
    },
    openCreate() { this.form = { id: null, name: '', iconUrl: '', status: 1, sort: 0 }; this.showModal = true },
    edit(c) { this.form = { ...c }; this.showModal = true },
    close() { this.showModal = false },
    async toggle(c) {
      try {
        await categoryApi.update({ id: c.id, status: c.status === 1 ? 0 : 1 })
        this.load()
      } catch (e) {}
    },
    chooseIcon() {
      uni.chooseImage({ count: 1, success: async (res) => {
        try { this.form.iconUrl = await fileApi.uploadIcon(res.tempFilePaths[0]) } catch (e) {}
      }})
    },
    async save() {
      if (!this.form.name) { uni.showToast({ title: '请填写分类名称', icon: 'none' }); return }
      try {
        if (this.form.id) await categoryApi.update(this.form)
        else await categoryApi.add(this.form)
        uni.showToast({ title: '已保存', icon: 'success' })
        this.showModal = false
        this.load()
      } catch (e) {}
    }
  }
}
</script>

<style scoped>
.container { min-height: 100vh; padding: 20rpx; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom: 20rpx; }
.title { font-size: 32rpx; color:#2E2A27; font-weight: bold; }
.category-list { display:flex; flex-direction: column; gap: 20rpx; }
.category-item { display:flex; align-items:center; gap: 20rpx; border-radius: 24rpx; box-shadow: 0 8rpx 24rpx rgba(123,91,68,0.06); }
.icon { width: 80rpx; height: 80rpx; border-radius: 12rpx; background:#EFE7DD; display:flex; align-items:center; justify-content:center; }
.info { flex:1; display:flex; align-items:center; gap: 16rpx; }
.name { font-size: 30rpx; color:#2E2A27; font-weight: bold; }
.status { font-size: 24rpx; color:#A39A92; }
.status.on { color:#7BB662; }
.actions { display:flex; gap: 12rpx; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.35); display:flex; align-items:center; justify-content:center; z-index:2000; }
.modal-card { width: 640rpx; }
.modal-title { font-size: 32rpx; font-weight: bold; color:#2E2A27; margin-bottom: 20rpx; }
.modal-body { display:flex; flex-direction: column; gap: 20rpx; }
.modal-input { background:#F6F3EF; border-radius: 16rpx; padding: 20rpx; font-size: 28rpx; }
.upload-row { display:flex; align-items:center; gap: 16rpx; }
.icon-preview { width: 64rpx; height: 64rpx; border-radius: 12rpx; background:#EFE7DD; }
.modal-actions { display:flex; gap: 20rpx; margin-top: 12rpx; }
.fab-button { position: fixed; right: 40rpx; bottom: 120rpx; width: 100rpx; height: 100rpx; border-radius: 50%; display:flex; align-items:center; justify-content:center; box-shadow: 0 10rpx 28rpx rgba(123,91,68,0.30); z-index: 1500; }
.fab-icon { color:#fff; font-size: 60rpx; font-weight: bold; }
</style>


