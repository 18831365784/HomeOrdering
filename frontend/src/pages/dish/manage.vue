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
          
          <!-- 扩展项设置（仅可视化） -->
          <view class="extensions-editor">
            <view class="viz-editor">
              <view class="viz-toolbar">
                <button class="small-btn" @click="addOption">＋ 添加选项</button>
              </view>
              <view v-if="vizOptions.length===0" class="text-muted">暂无选项，点击“添加选项”开始配置</view>
              <view v-for="(opt, oi) in vizOptions" :key="opt.__uid" class="viz-option">
                <view class="row">
                  <text class="option-id-label">选项ID: {{ opt.id }}</text>
                  <input class="modal-input" placeholder="选项名称(如：辣度)" v-model="opt.name" />
                </view>
                <view class="row">
                  <picker :range="selectionTypeLabels" :value="selectionTypes.indexOf(opt.selectionType)" @change="e=>changeType(oi, e.detail.value)">
                    <view class="picker-text">类型：{{ getSelectionTypeLabel(opt.selectionType) }}</view>
                  </picker>
                  <label class="switch">
                    <switch :checked="!!opt.required" @change="e=>opt.required=e.detail.value" /> 必填
                  </label>
                </view>
                <view v-if="opt.selectionType==='multiple'" class="row">
                  <input class="modal-input" type="number" placeholder="最少(min)" v-model.number="opt.min" />
                  <input class="modal-input" type="number" placeholder="最多(max)" v-model.number="opt.max" />
                </view>
                <view v-if="opt.selectionType==='input'" class="row">
                  <input class="modal-input" placeholder="占位提示" v-model="opt.placeholder" />
                </view>
                <view v-if="opt.selectionType==='number'" class="row">
                  <input class="modal-input" placeholder="单位(如：杯/份)" v-model="opt.unit" />
                  <input class="modal-input" placeholder="占位提示" v-model="opt.placeholder" />
                </view>
                
                <view v-if="opt.selectionType==='single' || opt.selectionType==='multiple'" class="choices">
                  <view class="choices-header">
                    <text>选择项</text>
                    <button class="small-btn" @click="addChoice(oi)">＋ 添加选择项</button>
                  </view>
                  <view v-if="!Array.isArray(opt.choices) || opt.choices.length===0" class="text-muted">暂无选择项</view>
                  <view v-for="(c, ci) in (opt.choices||[])" :key="c.__uid" class="choice-row">
                    <text class="choice-id-label">选择项ID: {{ c.id }}</text>
                    <input class="modal-input" placeholder="选择项名称" v-model="c.name" />
                    <input class="modal-input" type="number" placeholder="加价(可选)" v-model.number="c.price" />
                    <button class="tiny-btn" @click="moveChoice(oi, ci, -1)">↑</button>
                    <button class="tiny-btn" @click="moveChoice(oi, ci, 1)">↓</button>
                    <button class="tiny-btn danger" @click="removeChoice(oi, ci)">删</button>
                  </view>
                </view>

                <view class="opt-actions">
                  <button class="tiny-btn" @click="moveOption(oi, -1)">↑上移</button>
                  <button class="tiny-btn" @click="moveOption(oi, 1)">↓下移</button>
                  <button class="tiny-btn danger" @click="removeOption(oi)">删除选项</button>
                </view>
                <view class="divider" />
              </view>
            </view>
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
      form: { id: null, name: '', price: '', description: '', imageUrl: '', categoryId: null, status: 1, sort: 0, extensions: '' },
      useVisualEditor: true,
      vizOptions: [],
      selectionTypes: ['single','multiple','input','number','boolean','free_list'],
      selectionTypeLabels: ['单选','多选','文本输入','数字输入','开关','自由列表'],
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
      
      // 仅可视化：把 vizOptions 序列化为 JSON 存储
      if (true) {
        const json = { options: this.vizOptions.map(o => this.stripRuntimeFields(o)) }
        this.form.extensions = JSON.stringify(json)
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
.modal-card { width: 640rpx; max-height: 80vh; display: flex; flex-direction: column; }
.modal-body { flex: 1; overflow-y: auto; }
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

.extensions-editor { display: flex; flex-direction: column; gap: 12rpx; margin-top: 20rpx; }
.extensions-editor .label { font-size: 28rpx; color: #2E2A27; font-weight: bold; }
.extensions-editor textarea { min-height: 120rpx; font-family: monospace; font-size: 24rpx; }
.text-muted { font-size: 22rpx; color: #A39A92; }

.editor-tabs { display:flex; gap: 12rpx; margin-bottom: 8rpx; }
.tab-btn { background:#F6F3EF; color:#6A625B; border:none; border-radius: 16rpx; padding: 10rpx 20rpx; font-size: 24rpx; }
.tab-btn.active { background:#7B5B44; color:#fff; }
.viz-toolbar { display:flex; justify-content:flex-end; margin-bottom: 8rpx; }
.small-btn { background:#F6F3EF; color:#6A625B; border:none; border-radius: 16rpx; padding: 10rpx 20rpx; font-size: 24rpx; }
.tiny-btn { background:#F6F3EF; color:#6A625B; border:none; border-radius: 12rpx; padding: 8rpx 12rpx; font-size: 22rpx; }
.tiny-btn.danger { background:#F7E9E9; color:#B85C5C; }
.viz-option { background:#FBF9F6; border: 2rpx solid #E2D8CC; border-radius: 16rpx; padding: 16rpx; display:flex; flex-direction:column; gap: 12rpx; }
.viz-option .row { display:flex; gap: 12rpx; }
.choices { display:flex; flex-direction: column; gap: 8rpx; }
.choices-header { display:flex; justify-content:space-between; align-items:center; }
.choice-row { display:flex; gap: 8rpx; align-items:center; }
.opt-actions { display:flex; gap: 8rpx; }
.divider { height: 1rpx; background:#EDE7DF; margin-top: 8rpx; }
.choice-id-label { font-size: 22rpx; color: #999; background: #f5f5f5; padding: 8rpx 12rpx; border-radius: 8rpx; margin-bottom: 8rpx; display: block; }
.option-id-label { font-size: 22rpx; color: #999; background: #f5f5f5; padding: 8rpx 12rpx; border-radius: 8rpx; margin-bottom: 8rpx; display: block; }
</style>
