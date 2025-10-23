<template>
  <view class="container">
    <view class="header">
      <text class="title">菜品管理</text>
    </view>

    <view class="dish-list" 
          @touchmove="onDragMove($event)" 
          @touchend="onDragEnd($event)">
      <view class="dish-item card" 
            v-for="(dish, index) in dishes" 
            :key="dish.id"
            :class="{ 'dragging': dragStartIndex === index, 'drag-over': dragOverIndex === index }">
        <view class="sort-handle" 
              @touchstart.stop="onDragStart($event, index)">
          <text class="sort-icon">⋮⋮</text>
        </view>
        <image v-if="dish.imageUrl" class="dish-image" :src="dish.imageUrl" mode="aspectFit" />
        <view v-else class="dish-image placeholder-image">无</view>
        <view class="info">
          <text class="name">{{ dish.name }}</text>
          <text class="price">¥{{ dish.price }}</text>
          <text class="status" :class="{ on: dish.status === 1 }">{{ dish.status === 1 ? '上架' : '下架' }}</text>
        </view>
        <view class="actions">
          <button class="action-btn toggle-btn" :class="{ 'btn-on': dish.status === 1 }" @click="toggle(dish)">
            {{ dish.status === 1 ? '下架' : '上架' }}
          </button>
          <button class="action-btn edit-btn" @click="edit(dish)">编辑</button>
        </view>
      </view>
    </view>

    <!-- 编辑/新增弹窗 -->
    <view v-if="showModal" class="modal-mask" @touchmove.stop>
      <view class="modal-card card">
        <view class="modal-title">{{ form.id ? '编辑菜品' : '新增菜品' }}</view>
        <view class="modal-body">
          <input class="modal-input" placeholder="菜品名称" v-model="form.name" />
          <input class="modal-input" placeholder="菜品价格" v-model="form.price" type="number" />
          <input class="modal-input" placeholder="菜品描述" v-model="form.description" />
          <view class="upload-row">
            <button class="upload-btn" @click="chooseImage">选择图片</button>
            <text class="text-muted" v-if="!form.imageUrl">未选择</text>
            <image v-else class="image-preview" :src="form.imageUrl" mode="aspectFit" />
          </view>
          <view class="category-select">
            <text class="label">分类：</text>
            <picker @change="onCategoryChange" :value="categoryIndex" :range="categoryNames">
              <view class="picker-text">{{ selectedCategoryName }}</view>
            </picker>
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
import { dishApi, categoryApi, fileApi } from '@/utils/api.js'

export default {
  data() {
    return {
      dishes: [],
      categories: [],
      categoryNames: [],
      categoryIndex: 0,
      showModal: false,
      form: { id: null, name: '', price: '', description: '', imageUrl: '', categoryId: null, status: 1, sort: 0 },
      dragStartIndex: -1,
      dragOverIndex: -1,
      isDragging: false
    }
  },
  computed: {
    selectedCategoryName() {
      return this.categoryNames[this.categoryIndex] || '请选择分类'
    }
  },
  onShow() { 
    this.load() 
    this.loadCategories()
  },
  methods: {
    async load() {
      try {
        this.dishes = await dishApi.getList()
        // 按sort字段排序
        this.dishes.sort((a, b) => (a.sort || 0) - (b.sort || 0))
      } catch (e) { console.error(e) }
    },
    
    async loadCategories() {
      try {
        const list = await categoryApi.getList(1)
        this.categories = list
        this.categoryNames = list.map(c => c.name)
      } catch (e) { console.error(e) }
    },
    
    openCreate() { 
      this.form = { id: null, name: '', price: '', description: '', imageUrl: '', categoryId: null, category: '', status: 1, sort: 0 }
      this.categoryIndex = 0
      this.form.categoryId = this.categories[0].id
      this.form.category = this.categories[0].name
      this.showModal = true 
    },
    
    edit(dish) { 
      this.form = { ...dish }
      this.categoryIndex = this.categories.findIndex(c => c.name === dish.category)
      if (this.categoryIndex === -1) {
        this.categoryIndex = 0
        this.form.categoryId = this.categories[0].id
        this.form.category = this.categories[0].name
      } else {
        this.form.categoryId = this.categories[this.categoryIndex].id
      }
      this.showModal = true 
    },
    
    close() { this.showModal = false },
    
    async toggle(dish) {
      try {
        await dishApi.update({ id: dish.id, status: dish.status === 1 ? 0 : 1 })
        this.load()
      } catch (e) {}
    },
    
    chooseImage() {
      uni.chooseImage({ count: 1, success: async (res) => {
        try { this.form.imageUrl = await fileApi.upload(res.tempFilePaths[0]) } catch (e) {}
      }})
    },
    
    onCategoryChange(e) {
      this.categoryIndex = e.detail.value
      this.form.categoryId = this.categories[this.categoryIndex].id
      this.form.category = this.categories[this.categoryIndex].name // 设置分类名称
      console.log('分类变更:', this.categoryIndex, this.form.categoryId, this.form.category)
    },
    
    async save() {
      if (!this.form.name) { uni.showToast({ title: '请填写菜品名称', icon: 'none' }); return }
      if (!this.form.price) { uni.showToast({ title: '请填写菜品价格', icon: 'none' }); return }
      if (!this.form.categoryId) { uni.showToast({ title: '请选择分类', icon: 'none' }); return }
      
      // 确保分类名称正确设置
      if (!this.form.category && this.form.categoryId) {
        const selectedCategory = this.categories.find(c => c.id === this.form.categoryId)
        if (selectedCategory) {
          this.form.category = selectedCategory.name
        }
      }
      
      console.log('保存菜品数据:', this.form)
      
      try {
        if (this.form.id) {
          await dishApi.update(this.form)
        } else {
          // 新增时设置sort为当前最大sort+1
          const maxSort = Math.max(...this.dishes.map(d => d.sort || 0), 0)
          this.form.sort = maxSort + 1
          await dishApi.add(this.form)
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
    },
    
    onDragMove(e) {
      if (!this.isDragging) return
      
      // 获取触摸位置
      const touch = e.touches[0]
      if (!touch) return
      
      // 计算当前触摸位置对应的菜品索引
      const touchY = touch.clientY
      const dishHeight = 120 // 每个菜品项的大概高度
      const index = Math.floor(touchY / dishHeight)
      
      // 限制索引范围
      const validIndex = Math.max(0, Math.min(index, this.dishes.length - 1))
      
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
        await this.reorderDishes(this.dragStartIndex, this.dragOverIndex)
      } else {
        console.log('不执行排序：dragOverIndex=', this.dragOverIndex, 'dragStartIndex=', this.dragStartIndex)
      }
      
      // 重置状态
      this.dragStartIndex = -1
      this.dragOverIndex = -1
      this.isDragging = false
    },
    
    async reorderDishes(fromIndex, toIndex) {
      try {
        // 移动数组元素
        const item = this.dishes.splice(fromIndex, 1)[0]
        this.dishes.splice(toIndex, 0, item)
        
        // 重新计算sort值
        this.dishes.forEach((dish, index) => {
          dish.sort = index + 1
        })
        
        // 批量更新后端
        const updatePromises = this.dishes.map(dish => 
          dishApi.update({ id: dish.id, sort: dish.sort })
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
.dish-list { display:flex; flex-direction: column; gap: 20rpx; }
.dish-item { display:flex; align-items:center; gap: 16rpx; border-radius: 24rpx; box-shadow: 0 8rpx 24rpx rgba(123,91,68,0.06); padding: 20rpx; }
.sort-handle { display:flex; align-items:center; justify-content:center; width: 40rpx; height: 40rpx; color: #A39A92; cursor: grab; touch-action: none; }
.sort-icon { font-size: 24rpx; line-height: 1; }
.dragging { opacity: 0.5; transform: scale(1.05); }
.drag-over { border: 2rpx dashed #7B5B44; }
.dish-image { width: 80rpx; height: 80rpx; border-radius: 12rpx; background:#EFE7DD; display:flex; align-items:center; justify-content:center; flex-shrink: 0; }
.info { flex:1; display:flex; flex-direction: column; gap: 8rpx; }
.name { font-size: 30rpx; color:#2E2A27; font-weight: bold; }
.price { font-size: 28rpx; color:#7B5B44; font-weight: bold; }
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
.image-preview { width: 64rpx; height: 64rpx; border-radius: 12rpx; background:#EFE7DD; }
.category-select { display:flex; align-items:center; gap: 16rpx; }
.label { font-size: 28rpx; color:#2E2A27; }
.picker-text { background:#F6F3EF; border-radius: 16rpx; padding: 20rpx; font-size: 28rpx; color:#2E2A27; }
.modal-actions { display:flex; gap: 16rpx; margin-top: 12rpx; }
.modal-btn { padding: 16rpx 32rpx; border-radius: 24rpx; font-size: 28rpx; border: none; min-width: 120rpx; }
.cancel-btn { background: #F6F3EF; color: #6A625B; }
.save-btn { background: #7B5B44; color: #fff; }
.fab-button { position: fixed; right: 40rpx; bottom: 120rpx; width: 100rpx; height: 100rpx; border-radius: 50%; display:flex; align-items:center; justify-content:center; box-shadow: 0 10rpx 28rpx rgba(123,91,68,0.30); z-index: 1500; }
.fab-icon { color:#fff; font-size: 60rpx; font-weight: bold; }
</style>
