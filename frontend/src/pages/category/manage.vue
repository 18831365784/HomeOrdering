<template>
  <view class="container">
    <view class="header">
      <text class="title">分类管理</text>
    </view>

    <view class="category-list" 
          @touchmove="onDragMove($event)" 
          @touchend="onDragEnd($event)">
      <view class="category-item card" 
            v-for="(c, index) in categories" 
            :key="c.id"
            :class="{ 'dragging': dragStartIndex === index, 'drag-over': dragOverIndex === index }">
        <view class="sort-handle" 
              @touchstart.stop="onDragStart($event, index)">
          <text class="sort-icon">⋮⋮</text>
        </view>
        <image v-if="c.iconUrl" class="icon" :src="c.iconUrl" mode="aspectFit" />
        <view v-else class="icon placeholder-image">无</view>
        <view class="info">
          <text class="name">{{ c.name }}</text>
          <text class="status" :class="{ on: c.status === 1 }">{{ c.status === 1 ? '上架' : '下架' }}</text>
        </view>
        <view class="actions">
          <button class="action-btn toggle-btn" :class="{ 'btn-on': c.status === 1 }" @click="toggle(c)">
            {{ c.status === 1 ? '下架' : '上架' }}
          </button>
          <button class="action-btn edit-btn" @click="edit(c)">编辑</button>
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
            <button class="upload-btn" @click="chooseIcon">选择图标</button>
            <text class="text-muted" v-if="!form.iconUrl">未选择</text>
            <image v-else class="icon-preview" :src="form.iconUrl" mode="aspectFit" />
          </view>
        </view>
        <view class="modal-actions">
          <button class="modal-btn cancel-btn" @click="close">取消</button>
          <button class="modal-btn save-btn" @click="save">保存</button>
        </view>
      </view>
    </view>

    <!-- 右下角浮动新增按钮 -->
    <view class="fab-button primary-bg" @click="openCreate">
      <text class="fab-icon">＋</text>
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
      form: { id: null, name: '', iconUrl: '', status: 1, sort: 0 },
      dragStartIndex: -1,
      dragOverIndex: -1,
      isDragging: false
    }
  },
  onShow() { this.load() },
  methods: {
    async load() {
      try {
        this.categories = await categoryApi.getList(null)
        // 按sort字段排序
        this.categories.sort((a, b) => (a.sort || 0) - (b.sort || 0))
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
        if (this.form.id) {
          await categoryApi.update(this.form)
        } else {
          // 新增时设置sort为当前最大sort+1
          const maxSort = Math.max(...this.categories.map(c => c.sort || 0), 0)
          this.form.sort = maxSort + 1
          await categoryApi.add(this.form)
        }
        uni.showToast({ title: '已保存', icon: 'success' })
        this.showModal = false
        this.load()
      } catch (e) {}
    },
    
    // 拖拽排序相关方法
    onDragStart(e, index) {
      console.log('拖拽开始', index)
      this.dragStartIndex = index
      this.isDragging = true
      // 不阻止默认行为，让触摸事件正常工作
    },
    
    onDragMove(e) {
      if (!this.isDragging) return
      
      // 获取触摸位置
      const touch = e.touches[0]
      if (!touch) return
      
      // 计算当前触摸位置对应的分类索引
      const touchY = touch.clientY
      const categoryHeight = 120 // 每个分类项的大概高度（rpx转px约60px）
      const index = Math.floor(touchY / categoryHeight)
      
      // 限制索引范围
      const validIndex = Math.max(0, Math.min(index, this.categories.length - 1))
      
      console.log('拖拽移动', 'touchY:', touchY, '计算索引:', validIndex, '当前dragOverIndex:', this.dragOverIndex)
      
      if (validIndex !== this.dragOverIndex) {
        this.dragOverIndex = validIndex
      }
    },
    
    async onDragEnd(e) {
      if (!this.isDragging) return
      console.log('拖拽结束', '从', this.dragStartIndex, '到', this.dragOverIndex)
      
      // 如果拖拽目标有效且与起始位置不同，执行排序
      if (this.dragOverIndex !== -1 && this.dragStartIndex !== this.dragOverIndex) {
        console.log('执行排序：从', this.dragStartIndex, '到', this.dragOverIndex)
        await this.reorderCategories(this.dragStartIndex, this.dragOverIndex)
      } else {
        console.log('不执行排序：dragOverIndex=', this.dragOverIndex, 'dragStartIndex=', this.dragStartIndex)
      }
      
      // 重置状态
      this.dragStartIndex = -1
      this.dragOverIndex = -1
      this.isDragging = false
    },
    
    async reorderCategories(fromIndex, toIndex) {
      try {
        // 移动数组元素
        const item = this.categories.splice(fromIndex, 1)[0]
        this.categories.splice(toIndex, 0, item)
        
        // 重新计算sort值
        this.categories.forEach((category, index) => {
          category.sort = index + 1
        })
        
        // 批量更新后端
        const updatePromises = this.categories.map(category => 
          categoryApi.update({ id: category.id, sort: category.sort })
        )
        
        await Promise.all(updatePromises)
        uni.showToast({ title: '排序已保存', icon: 'success' })
      } catch (error) {
        console.error('排序失败:', error)
        uni.showToast({ title: '排序失败', icon: 'none' })
        // 重新加载数据
        this.load()
      }
    }
  }
}
</script>

<style scoped>
.container { min-height: 100vh; padding: 20rpx; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom: 20rpx; }
.title { font-size: 32rpx; color:#2E2A27; font-weight: bold; }
.category-list { display:flex; flex-direction: column; gap: 20rpx; }
.category-item { display:flex; align-items:center; gap: 16rpx; border-radius: 24rpx; box-shadow: 0 8rpx 24rpx rgba(123,91,68,0.06); padding: 20rpx; }
.sort-handle { display:flex; align-items:center; justify-content:center; width: 40rpx; height: 40rpx; color: #A39A92; cursor: grab; touch-action: none; }
.sort-icon { font-size: 24rpx; line-height: 1; }
.dragging { opacity: 0.5; transform: scale(1.05); }
.drag-over { border: 2rpx dashed #7B5B44; }
.icon { width: 80rpx; height: 80rpx; border-radius: 12rpx; background:#EFE7DD; display:flex; align-items:center; justify-content:center; flex-shrink: 0; }
.info { flex:1; display:flex; flex-direction: column; gap: 8rpx; }
.name { font-size: 30rpx; color:#2E2A27; font-weight: bold; }
.status { font-size: 24rpx; color:#A39A92; }
.status.on { color:#7BB662; }
.actions { display:flex; gap: 8rpx; }
.action-btn { padding: 12rpx 20rpx; border-radius: 20rpx; font-size: 24rpx; border: none; min-width: 80rpx; }
.toggle-btn { background: #F6F3EF; color: #6A625B; }
.toggle-btn.btn-on { background: #7BB662; color: #fff; }
.edit-btn { background: #7B5B44; color: #fff; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.35); display:flex; align-items:center; justify-content:center; z-index:2000; }
.modal-card { width: 640rpx; }
.modal-title { font-size: 32rpx; font-weight: bold; color:#2E2A27; margin-bottom: 20rpx; }
.modal-body { display:flex; flex-direction: column; gap: 20rpx; }
.modal-input { background:#F6F3EF; border-radius: 16rpx; padding: 20rpx; font-size: 28rpx; }
.upload-row { display:flex; align-items:center; gap: 16rpx; }
.upload-btn { padding: 12rpx 20rpx; border-radius: 20rpx; font-size: 24rpx; background: #F6F3EF; color: #6A625B; border: none; }
.icon-preview { width: 64rpx; height: 64rpx; border-radius: 12rpx; background:#EFE7DD; }
.modal-actions { display:flex; gap: 16rpx; margin-top: 12rpx; }
.modal-btn { padding: 16rpx 32rpx; border-radius: 24rpx; font-size: 28rpx; border: none; min-width: 120rpx; }
.cancel-btn { background: #F6F3EF; color: #6A625B; }
.save-btn { background: #7B5B44; color: #fff; }
.fab-button { position: fixed; right: 40rpx; bottom: 120rpx; width: 100rpx; height: 100rpx; border-radius: 50%; display:flex; align-items:center; justify-content:center; box-shadow: 0 10rpx 28rpx rgba(123,91,68,0.30); z-index: 1500; }
.fab-icon { color:#fff; font-size: 60rpx; font-weight: bold; }
</style>


