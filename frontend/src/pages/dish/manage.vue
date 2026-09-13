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
        <SafeImage v-if="dish.imageUrl" :src="dish.imageUrl" mode="aspectFit" imgClass="dish-image" />
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
    <view v-if="showModal" class="modal-mask" @touchmove.stop.prevent>
      <view class="modal-card card" @touchmove.stop>
        <view class="modal-title">{{ form.id ? '编辑菜品' : '新增菜品' }}</view>
        <scroll-view class="modal-body" scroll-y="true" enable-back-to-top="true">
          <input class="modal-input" placeholder="菜品名称" v-model="form.name" />
          <input class="modal-input" placeholder="菜品价格" v-model="form.price" type="number" />
          <input class="modal-input" placeholder="菜品描述" v-model="form.description" />
          <view class="upload-row">
            <button class="upload-btn" @click="chooseImage">选择图片</button>
            <text class="text-muted" v-if="!form.imageUrl">未选择</text>
            <SafeImage v-else :src="form.imageUrl" mode="aspectFit" imgClass="image-preview" />
          </view>
          <view class="category-select">
            <text class="label">分类：</text>
            <picker @change="onCategoryChange" :value="categoryIndex" :range="categoryNames">
              <view class="picker-text">{{ selectedCategoryName }}</view>
            </picker>
          </view>
          
          <!-- 扩展项设置（仅可视化） -->
          <view class="extensions-editor">
            <view class="editor-header">
              <text class="editor-title">扩展选项配置</text>
              <button class="add-option-btn" @click="addOption">
                <text class="btn-icon">＋</text>
                <text class="btn-text">添加选项</text>
              </button>
            </view>
            
            <view v-if="vizOptions.length===0" class="empty-state">
              <text class="empty-icon">📝</text>
              <text class="empty-text">暂无扩展选项</text>
              <text class="empty-desc">点击上方"添加选项"开始配置</text>
            </view>
            
            <view v-for="(opt, oi) in vizOptions" :key="opt.__uid" class="option-card">
              <!-- 选项头部 -->
              <view class="option-header">
                <view class="option-title-row">
                  <input class="option-name-input" placeholder="选项名称(如：辣度)" v-model="opt.name" />
                  <view class="option-type-picker">
                    <picker :range="selectionTypeLabels" :value="selectionTypes.indexOf(opt.selectionType)" @change="e=>changeType(oi, e.detail.value)">
                      <view class="type-picker-text">{{ getSelectionTypeLabel(opt.selectionType) }}</view>
                    </picker>
                  </view>
                </view>
                <view class="option-required">
                  <label class="required-switch">
                    <switch :checked="!!opt.required" @change="e=>opt.required=e.detail.value" />
                    <text class="required-label">必填</text>
                  </label>
                </view>
              </view>
              
              <!-- 选项配置 -->
              <view class="option-config">
                <view v-if="opt.selectionType==='multiple'" class="config-row">
                  <view class="config-item">
                    <text class="config-label">最少选择</text>
                    <input class="config-input" type="number" placeholder="0" v-model.number="opt.min" />
                  </view>
                  <view class="config-item">
                    <text class="config-label">最多选择</text>
                    <input class="config-input" type="number" placeholder="无限制" v-model.number="opt.max" />
                  </view>
                </view>
                
                <view v-if="opt.selectionType==='input'" class="config-row">
                  <view class="config-item full-width">
                    <text class="config-label">占位提示</text>
                    <input class="config-input" placeholder="请输入..." v-model="opt.placeholder" />
                  </view>
                </view>
                
                <view v-if="opt.selectionType==='number'" class="config-row">
                  <view class="config-item">
                    <text class="config-label">单位</text>
                    <input class="config-input" placeholder="杯/份" v-model="opt.unit" />
                  </view>
                  <view class="config-item">
                    <text class="config-label">占位提示</text>
                    <input class="config-input" placeholder="请输入数字" v-model="opt.placeholder" />
                  </view>
                </view>
              </view>
              
              <!-- 选择项配置 -->
              <view v-if="opt.selectionType==='single' || opt.selectionType==='multiple'" class="choices-section">
                <view class="choices-header">
                  <text class="choices-title">选择项</text>
                  <button class="add-choice-btn" @click="addChoice(oi)">
                    <text class="btn-icon">＋</text>
                    <text class="btn-text">添加选择项</text>
                  </button>
                </view>
                
                <view v-if="!Array.isArray(opt.choices) || opt.choices.length===0" class="choices-empty">
                  <text class="empty-text">暂无选择项</text>
                </view>
                
                <view v-for="(c, ci) in (opt.choices||[])" :key="c.__uid" class="choice-item">
                  <view class="choice-content">
                    <input class="choice-name-input" placeholder="选择项名称" v-model="c.name" />
                    <input class="choice-price-input" type="number" placeholder="加价" v-model.number="c.price" />
                  </view>
                  <view class="choice-actions">
                    <button class="action-btn move-up" @click="moveChoice(oi, ci, -1)" :disabled="ci === 0">
                      <text>↑</text>
                    </button>
                    <button class="action-btn move-down" @click="moveChoice(oi, ci, 1)" :disabled="ci === opt.choices.length - 1">
                      <text>↓</text>
                    </button>
                    <button class="action-btn delete" @click="removeChoice(oi, ci)">
                      <text>×</text>
                    </button>
                  </view>
                </view>
              </view>
              
              <!-- 选项操作 -->
              <view class="option-actions">
                <button class="action-btn move-up" @click="moveOption(oi, -1)" :disabled="oi === 0">
                  <text>↑</text>
                  <text class="btn-text">上移</text>
                </button>
                <button class="action-btn move-down" @click="moveOption(oi, 1)" :disabled="oi === vizOptions.length - 1">
                  <text>↓</text>
                  <text class="btn-text">下移</text>
                </button>
                <button class="action-btn delete" @click="removeOption(oi)">
                  <text>×</text>
                  <text class="btn-text">删除</text>
                </button>
              </view>
            </view>
          </view>
        </scroll-view>
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
import SafeImage from '@/components/SafeImage.vue'
import userManager from '@/utils/user.js'

export default {
  components: { SafeImage },
  data() {
    return {
      dishes: [],
      categories: [],
      categoryNames: [],
      categoryIndex: 0,
      showModal: false,
      uuid: '',
      form: { id: null, name: '', price: '', description: '', imageUrl: '', categoryId: null, status: 1, sort: 0, extensions: '' },
      useVisualEditor: true,
      vizOptions: [],
      selectionTypes: ['single','multiple','input','number','boolean'],
      selectionTypeLabels: ['单选','多选','文本输入','数字输入','开关'],
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
  onLoad() {
    this.uuid = userManager.getUuid()
  },
  onShow() {
    this.load()
    this.loadCategories()
  },
  methods: {
    // 可视化编辑器：帮助函数
    parseExtensionsToViz(extensions) {
      try {
        const obj = typeof extensions === 'string' ? JSON.parse(extensions || '{}') : (extensions || {})
        const options = Array.isArray(obj.options) ? obj.options : []
        return options.map(o => {
          const option = this.withUids({ selectionType: 'single', required: false, choices: [], ...o })
          // 如果没有ID，生成一个
          if (!option.id) {
            option.id = this.generateOptionId()
          }
          // 为选择项生成ID
          if (Array.isArray(option.choices)) {
            option.choices.forEach(choice => {
              if (!choice.id) {
                choice.id = `choice_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`
              }
            })
          }
          return option
        })
      } catch (e) {
        return []
      }
    },
    withUids(opt) {
      const uid = Math.random().toString(36).slice(2, 9)
      const out = { __uid: uid, ...opt }
      if (Array.isArray(out.choices)) {
        out.choices = out.choices.map(c => ({ __uid: Math.random().toString(36).slice(2, 9), ...c }))
      }
      return out
    },
    stripRuntimeFields(opt) {
      const { __uid, ...pure } = opt
      if (Array.isArray(pure.choices)) {
        pure.choices = pure.choices.map(c => { const { __uid, ...pc } = c; return pc })
      }
      return pure
    },
    changeType(oi, idx) { this.vizOptions[oi].selectionType = this.selectionTypes[idx] },
    getSelectionTypeLabel(type) {
      const index = this.selectionTypes.indexOf(type)
      return index >= 0 ? this.selectionTypeLabels[index] : type
    },
    generateOptionId() {
      // 生成唯一的选项ID
      const timestamp = Date.now().toString(36)
      const random = Math.random().toString(36).substring(2, 8)
      return `opt_${timestamp}_${random}`
    },
    addOption() { 
      const newOption = this.withUids({ 
        id: this.generateOptionId(), 
        name: '', 
        selectionType: 'single', 
        required: false, 
        choices: [] 
      })
      this.vizOptions.push(newOption)
    },
    removeOption(oi) { this.vizOptions.splice(oi, 1) },
    moveOption(oi, delta) {
      const ni = oi + delta
      if (ni < 0 || ni >= this.vizOptions.length) return
      const item = this.vizOptions.splice(oi, 1)[0]
      this.vizOptions.splice(ni, 0, item)
    },
    addChoice(oi) {
      const opt = this.vizOptions[oi]
      if (!Array.isArray(opt.choices)) opt.choices = []
      const choiceId = `choice_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`
      opt.choices.push({ __uid: Math.random().toString(36).slice(2, 9), id: choiceId, name: '', price: 0 })
    },
    removeChoice(oi, ci) { const opt = this.vizOptions[oi]; if (!opt || !opt.choices) return; opt.choices.splice(ci, 1) },
    moveChoice(oi, ci, delta) {
      const opt = this.vizOptions[oi]
      if (!opt || !Array.isArray(opt.choices)) return
      const ni = ci + delta
      if (ni < 0 || ni >= opt.choices.length) return
      const item = opt.choices.splice(ci, 1)[0]
      opt.choices.splice(ni, 0, item)
    },
    async load() {
      try {
        // 管理页面显示所有菜品，包括已下架的
        this.dishes = await dishApi.getList(null, this.uuid)
        // 按sort字段排序
        this.dishes.sort((a, b) => (a.sort || 0) - (b.sort || 0))
      } catch (e) { console.error(e) }
    },
    
    async loadCategories() {
      try {
        const list = await categoryApi.getList(1, this.uuid)
        this.categories = list
        this.categoryNames = list.map(c => c.name)
      } catch (e) { console.error(e) }
    },
    
    openCreate() { 
      this.form = { id: null, name: '', price: '', description: '', imageUrl: '', categoryId: null, category: '', status: 1, sort: 0, extensions: '' }
      this.categoryIndex = 0
      this.form.categoryId = this.categories[0].id
      this.form.category = this.categories[0].name
      this.vizOptions = []
      this.useVisualEditor = true
      this.showModal = true 
    },
    
    edit(dish) { 
      this.form = { ...dish }
      if (this.form.extensions && typeof this.form.extensions !== 'string') {
        try { this.form.extensions = JSON.stringify(this.form.extensions) } catch (e) { this.form.extensions = '' }
      }
      this.vizOptions = this.parseExtensionsToViz(this.form.extensions)
      this.useVisualEditor = true
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
        await dishApi.update({ id: dish.id, status: dish.status === 1 ? 0 : 1 }, this.uuid)
        this.load()
        // 通知首页刷新数据
        uni.$emit('dishUpdated')
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
      
      // 仅可视化：把 vizOptions 序列化为 JSON 存储
      if (true) {
        const json = { options: this.vizOptions.map(o => this.stripRuntimeFields(o)) }
        this.form.extensions = JSON.stringify(json)
      }
      console.log('保存菜品数据:', this.form)
      
      try {
        if (this.form.id) {
          await dishApi.update(this.form, this.uuid)
        } else {
          // 新增时设置sort为当前最大sort+1
          const maxSort = Math.max(...this.dishes.map(d => d.sort || 0), 0)
          this.form.sort = maxSort + 1
          await dishApi.add(this.form, this.uuid)
        }
        uni.showToast({ title: '已保存', icon: 'success' })
        this.showModal = false
        this.load()
        // 通知首页刷新数据
        uni.$emit('dishUpdated')
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
          dishApi.update({ id: dish.id, sort: dish.sort }, this.uuid)
        )
        
        await Promise.all(updatePromises)
        uni.showToast({ title: '排序已保存', icon: 'success' })
        // 通知首页刷新数据
        uni.$emit('dishUpdated')
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
:deep(.dish-image) { width: 80rpx; height: 80rpx; border-radius: 12rpx; }
.info { flex:1; display:flex; flex-direction: column; gap: 8rpx; }
.name { font-size: 30rpx; color:#2E2A27; font-weight: bold; }
.price { font-size: 28rpx; color:#7B5B44; font-weight: bold; }
.status { font-size: 24rpx; color:#A39A92; }
.status.on { color:#7BB662; }
.actions { display:flex; gap: 8rpx; }
.action-btn { padding: 3rpx 20rpx 0rpx; border-radius: 20rpx; font-size: 24rpx; border: none; min-width: 80rpx; }
.toggle-btn { background: #F6F3EF; color: #6A625B; }
.toggle-btn.btn-on { background: #7BB662; color: #fff; }
.edit-btn { background: #7B5B44; color: #fff; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.35); display:flex; align-items:center; justify-content:center; z-index:2000; }
.modal-card { width: 640rpx; max-height: 85vh; display: flex; flex-direction: column; background: #fff; border-radius: 24rpx; overflow: hidden; }
.modal-title { font-size: 32rpx; font-weight: bold; color:#2E2A27; margin: 20rpx 20rpx 0 20rpx; flex-shrink: 0; }
.modal-body { 
  /* 设定固定高度以启用 scroll-view 原生滚动（小程序需要明确高度） */
  height: 60vh;
  padding: 20rpx; 
  display: flex; 
  flex-direction: column; 
  overflow: hidden; /* 避免与 scroll-view 双重滚动冲突 */
}
.modal-actions { display:flex; gap: 16rpx; margin: 0 20rpx 20rpx 20rpx; flex-shrink: 0; }
.modal-input { 
  background: #F6F3EF; 
  border: 2rpx solid #E2D8CC;
  border-radius: 16rpx; 
  padding: 20rpx; 
  font-size: 28rpx; 
  color: #2E2A27;
  transition: all 0.2s ease;
  margin-bottom: 12rpx;
}

.modal-input:focus {
  border-color: #7B5B44;
  background: #ffffff;
  box-shadow: 0 0 0 4rpx rgba(123, 91, 68, 0.1);
}

.upload-row { 
  display: flex; 
  align-items: center; 
  gap: 16rpx; 
  margin-bottom: 12rpx;
}

.upload-btn { 
  padding: 0 24rpx; 
  height: 60rpx;
  border-radius: 16rpx; 
  font-size: 26rpx; 
  background: linear-gradient(135deg, #7B5B44 0%, #9F7A5A 100%);
  color: #fff; 
  border: none; 
  box-shadow: 0 4rpx 12rpx rgba(123, 91, 68, 0.25);
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-btn:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(123, 91, 68, 0.35);
}

.image-preview { 
  width: 80rpx; 
  height: 80rpx; 
  border-radius: 16rpx; 
  background: #EFE7DD;
  border: 2rpx solid #E2D8CC;
  object-fit: cover;
}
:deep(.image-preview) { width: 80rpx; height: 80rpx; border-radius: 16rpx; }

.category-select { 
  display: flex; 
  align-items: center; 
  gap: 16rpx; 
  margin-bottom: 12rpx;
}

.label { 
  font-size: 28rpx; 
  color: #2E2A27; 
  font-weight: 500;
}

.picker-text { 
  background: #F6F3EF; 
  border: 2rpx solid #E2D8CC;
  border-radius: 16rpx; 
  padding: 20rpx; 
  font-size: 28rpx; 
  color: #2E2A27;
  transition: all 0.2s ease;
}

.picker-text:active {
  border-color: #7B5B44;
  background: #ffffff;
}
.modal-btn { padding: 16rpx 32rpx; border-radius: 24rpx; font-size: 28rpx; border: none; min-width: 120rpx; }
.cancel-btn { background: #F6F3EF; color: #6A625B; }
.save-btn { background: #7B5B44; color: #fff; }
.fab-button { position: fixed; right: 40rpx; bottom: 120rpx; width: 100rpx; height: 100rpx; border-radius: 50%; display:flex; align-items:center; justify-content:center; box-shadow: 0 10rpx 28rpx rgba(123,91,68,0.30); z-index: 1500; }
.fab-icon { color:#fff; font-size: 60rpx; font-weight: bold; }

/* 扩展项编辑器样式 */
.extensions-editor { 
  margin-top: 24rpx; 
  display: flex; 
  flex-direction: column; 
  gap: 24rpx; 
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  width: 100%;
}

.editor-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #2E2A27;
}

.add-option-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: linear-gradient(135deg, #7B5B44 0%, #9F7A5A 100%);
  color: #fff;
  border: none;
  border-radius: 20rpx;
  padding: 0 20rpx;
  height: 60rpx;
  font-size: 26rpx;
  box-shadow: 0 4rpx 12rpx rgba(123, 91, 68, 0.25);
  margin: 0;
  flex-shrink: 0;
}

.btn-icon {
  font-size: 28rpx;
  font-weight: bold;
}

.btn-text {
  font-size: 26rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 20rpx;
  background: #F8F6F3;
  border-radius: 16rpx;
  border: 2rpx dashed #E2D8CC;
}

.empty-icon {
  font-size: 60rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #6A625B;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #A39A92;
}

.option-card {
  background: #ffffff;
  border: 2rpx solid #E2D8CC;
  border-radius: 20rpx;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(123, 91, 68, 0.06);
}

.option-header {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.option-title-row {
  display: flex;
  gap: 16rpx;
  align-items: center;
}

.option-name-input {
  flex: 1;
  background: #F6F3EF;
  border: 2rpx solid #E2D8CC;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  font-size: 28rpx;
  color: #2E2A27;
}

.option-type-picker {
  min-width: 160rpx;
}

.type-picker-text {
  background: #F6F3EF;
  border: 2rpx solid #E2D8CC;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  font-size: 26rpx;
  color: #2E2A27;
  text-align: center;
}

.option-required {
  display: flex;
  justify-content: flex-end;
}

.required-switch {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.required-switch switch {
  transform: scale(0.8);
}

.required-label {
  font-size: 24rpx;
  color: #6A625B;
  font-weight: 500;
}

.option-config {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.config-row {
  display: flex;
  gap: 16rpx;
}

.config-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.config-item.full-width {
  flex: 1;
}

.config-label {
  font-size: 24rpx;
  color: #6A625B;
  font-weight: 500;
}

.config-input {
  background: #F6F3EF;
  border: 2rpx solid #E2D8CC;
  border-radius: 12rpx;
  padding: 12rpx 16rpx;
  font-size: 26rpx;
  color: #2E2A27;
}

.choices-section {
  background: #F8F6F3;
  border-radius: 16rpx;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.choices-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.choices-title {
  font-size: 28rpx;
  color: #2E2A27;
  font-weight: bold;
}

.add-choice-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  background: #7B5B44;
  color: #fff;
  border: none;
  border-radius: 16rpx;
  padding: 0 16rpx;
  height: 48rpx;
  font-size: 24rpx;
  margin: 0;
  flex-shrink: 0;
}

.choices-empty {
  text-align: center;
  padding: 20rpx;
  color: #A39A92;
  font-size: 24rpx;
}

.choice-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: #ffffff;
  border-radius: 12rpx;
  padding: 16rpx;
  border: 1rpx solid #E2D8CC;
}

.choice-content {
  flex: 1;
  display: flex;
  gap: 12rpx;
}

.choice-name-input {
  flex: 1;
  background: #F6F3EF;
  border: 1rpx solid #E2D8CC;
  border-radius: 8rpx;
  padding: 12rpx 16rpx;
  font-size: 26rpx;
  color: #2E2A27;
}

.choice-price-input {
  width: 120rpx;
  background: #F6F3EF;
  border: 1rpx solid #E2D8CC;
  border-radius: 8rpx;
  padding: 12rpx 16rpx;
  font-size: 26rpx;
  color: #2E2A27;
  text-align: center;
}

.choice-actions {
  display: flex;
  gap: 6rpx;
}

.option-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #E2D8CC;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
  border: none;
  transition: all 0.2s ease;
}

.action-btn.move-up {
  background: #E8F4FD;
  color: #4A90E2;
}

.action-btn.move-down {
  background: #E8F4FD;
  color: #4A90E2;
}

.action-btn.delete {
  background: #F7E9E9;
  color: #B85C5C;
}

.action-btn:disabled {
  opacity: 0.4;
  background: #F0F0F0;
  color: #999;
}
</style>
