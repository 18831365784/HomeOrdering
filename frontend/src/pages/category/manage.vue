<template>
  <view class="page">
    <view
      class="list"
      @touchmove="onDragMove($event)"
      @touchend="onDragEnd($event)"
    >
      <view
        class="row"
        v-for="(c, index) in categories"
        :key="c.id"
        :class="{ dragging: dragStartIndex === index, 'drag-over': dragOverIndex === index }"
      >
        <view class="sort-handle" @touchstart.stop="onDragStart($event, index)">
          <AppIcon name="drag" :size="28" color="#9A9086" />
        </view>
        <SafeImage v-if="c.iconUrl" :src="c.iconUrl" mode="aspectFill" imgClass="icon" />
        <view v-else class="icon placeholder">
          <AppIcon name="tag" :size="28" color="#D4C9BC" />
        </view>
        <view class="info">
          <text class="name">{{ c.name }}</text>
          <text class="status" :class="{ on: c.status === 1 }">{{ c.status === 1 ? '上架' : '下架' }}</text>
        </view>
        <view class="actions">
          <button
            class="btn btn-sm"
            :class="c.status === 1 ? 'btn-ghost' : 'btn-secondary'"
            @click="toggle(c)"
          >{{ c.status === 1 ? '下架' : '上架' }}</button>
          <button class="btn btn-sm btn-primary" @click="edit(c)">编辑</button>
        </view>
      </view>
      <EmptyState v-if="categories.length === 0" icon="tag" text="暂无分类" />
    </view>

    <view class="fab" @click="openCreate">
      <AppIcon name="plus" :size="40" color="#FFFFFF" />
    </view>

    <AppModal
      :show="showModal"
      :title="form.id ? '编辑分类' : '新增分类'"
      confirm-text="保存"
      @update:show="showModal = $event"
      @confirm="save"
    >
      <view class="field">
        <text class="field-label">分类名称</text>
        <input class="input" placeholder="请输入分类名称" v-model="form.name" />
      </view>
      <view class="field">
        <text class="field-label">图标（选填）</text>
        <view class="upload-row">
          <button class="btn btn-sm btn-ghost" @click="chooseIcon">选择图标</button>
          <text v-if="!form.iconUrl" class="text-muted">未选择</text>
          <SafeImage v-else :src="form.iconUrl" mode="aspectFill" imgClass="preview" />
        </view>
      </view>
    </AppModal>
  </view>
</template>

<script>
import { categoryApi, fileApi } from '@/utils/api.js'
import SafeImage from '@/components/SafeImage.vue'
import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import AppModal from '@/components/AppModal.vue'
import userManager from '@/utils/user.js'

export default {
  components: { SafeImage, AppIcon, EmptyState, AppModal },
  data() {
    return {
      categories: [],
      showModal: false,
      form: { id: null, name: '', iconUrl: '', status: 1, sort: 0 },
      dragStartIndex: -1,
      dragOverIndex: -1,
      isDragging: false
    }
  },
  onShow() { this.load() },
  onLoad() {
    this.uuid = userManager.getUuid()
  },
  methods: {
    async load() {
      try {
        this.categories = await categoryApi.getList(null)
        this.categories.sort((a, b) => (a.sort || 0) - (b.sort || 0))
      } catch (e) { console.error(e) }
    },
    openCreate() {
      this.form = { id: null, name: '', iconUrl: '', status: 1, sort: 0 }
      this.showModal = true
    },
    edit(c) {
      this.form = { ...c }
      this.showModal = true
    },
    async toggle(c) {
      try {
        await categoryApi.update({ id: c.id, status: c.status === 1 ? 0 : 1 })
        this.load()
        uni.$emit('categoryUpdated')
      } catch (e) {}
    },
    chooseIcon() {
      uni.chooseImage({
        count: 1,
        success: async (res) => {
          try {
            this.form.iconUrl = await fileApi.uploadIcon(res.tempFilePaths[0])
          } catch (e) {}
        }
      })
    },
    async save() {
      if (!this.form.name) {
        uni.showToast({ title: '请填写分类名称', icon: 'none' })
        return
      }
      try {
        if (this.form.id) {
          await categoryApi.update(this.form)
        } else {
          const maxSort = Math.max(...this.categories.map(c => c.sort || 0), 0)
          this.form.sort = maxSort + 1
          await categoryApi.add(this.form)
        }
        uni.showToast({ title: '已保存', icon: 'success' })
        this.showModal = false
        this.load()
        uni.$emit('categoryUpdated')
      } catch (e) {}
    },
    onDragStart(e, index) {
      this.dragStartIndex = index
      this.isDragging = true
    },
    onDragMove(e) {
      if (!this.isDragging) return
      const touch = e.touches[0]
      if (!touch) return
      const index = Math.floor(touch.clientY / 60)
      this.dragOverIndex = Math.max(0, Math.min(index, this.categories.length - 1))
    },
    async onDragEnd() {
      if (!this.isDragging) return
      if (this.dragOverIndex !== -1 && this.dragStartIndex !== this.dragOverIndex) {
        await this.reorderCategories(this.dragStartIndex, this.dragOverIndex)
      }
      this.dragStartIndex = -1
      this.dragOverIndex = -1
      this.isDragging = false
    },
    async reorderCategories(fromIndex, toIndex) {
      try {
        const item = this.categories.splice(fromIndex, 1)[0]
        this.categories.splice(toIndex, 0, item)
        this.categories.forEach((category, index) => {
          category.sort = index + 1
        })
        await Promise.all(
          this.categories.map(category =>
            categoryApi.update({ id: category.id, sort: category.sort })
          )
        )
        uni.showToast({ title: '排序已保存', icon: 'success' })
        uni.$emit('categoryUpdated')
      } catch (error) {
        uni.showToast({ title: '排序失败', icon: 'none' })
        this.load()
      }
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100%;
  background: #F7F3EE;
  padding: 24rpx 24rpx 180rpx;
  box-sizing: border-box;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
}

.dragging { opacity: 0.55; }
.drag-over { border: 2rpx dashed #B85C38; }

.sort-handle {
  width: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.icon) {
  width: 72rpx;
  height: 72rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.placeholder {
  width: 72rpx;
  height: 72rpx;
  border-radius: 12rpx;
  background: #F7F3EE;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #2A2420;
}

.status {
  font-size: 22rpx;
  color: #9A9086;
}

.status.on {
  color: #5C6B5A;
}

.actions {
  display: flex;
  gap: 10rpx;
}

.fab {
  position: fixed;
  right: 40rpx;
  bottom: calc(48rpx + env(safe-area-inset-bottom));
  width: 104rpx;
  height: 104rpx;
  border-radius: 28rpx;
  background: #B85C38;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(184, 92, 56, 0.35);
  z-index: 100;
}

.field {
  margin-bottom: 32rpx;
}

.field-label {
  display: block;
  font-size: 24rpx;
  color: #9A9086;
  margin-bottom: 12rpx;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

:deep(.preview) {
  width: 64rpx;
  height: 64rpx;
  border-radius: 12rpx;
}
</style>
