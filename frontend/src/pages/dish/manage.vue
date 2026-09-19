<template>
  <view class="container">
    <view class="dish-list" 
          @touchmove="onDragMove($event)" 
          @touchend="onDragEnd($event)">
      <view class="dish-item card" 
            v-for="(dish, index) in dishes" 
            :key="dish.id"
            :class="{ 'dragging': dragStartIndex === index, 'drag-over': dragOverIndex === index }">
        <view class="sort-handle" 
              @touchstart.stop="onDragStart($event, index)">
          <AppIcon name="drag" :size="28" color="#9A9086" />
        </view>
        <SafeImage v-if="dish.imageUrl" :src="dish.imageUrl" mode="aspectFill" imgClass="dish-image" />
        <view v-else class="dish-image placeholder-image">
          <AppIcon name="dish" :size="28" color="#D4C9BC" />
        </view>
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
    <view v-if="showModal" class="modal-mask" @touchmove.stop.prevent @click="close">
      <view class="modal-card" @click.stop @touchmove.stop>
        <view class="modal-head">
          <view class="modal-head-text">
            <text class="modal-title">{{ form.id ? '编辑菜品' : '新增菜品' }}</text>
            <text class="modal-sub">填写基本信息与可选规格</text>
          </view>
          <view class="modal-close" @click="close">
            <AppIcon name="close" :size="28" color="#9A9086" />
          </view>
        </view>

        <scroll-view class="modal-body" scroll-y="true" :show-scrollbar="false">
          <view class="form-section">
            <text class="section-cap">基本信息</text>
            <view class="field">
              <text class="field-label">菜品名称</text>
              <input class="modal-input" placeholder="请输入名称" v-model="form.name" />
            </view>
            <view class="field-row">
              <view class="field half">
                <text class="field-label">价格（元）</text>
                <input class="modal-input" placeholder="0.00" v-model="form.price" type="digit" />
              </view>
              <view class="field half">
                <text class="field-label">分类</text>
                <picker @change="onCategoryChange" :value="categoryIndex" :range="categoryNames">
                  <view class="picker-text">{{ selectedCategoryName }}</view>
                </picker>
              </view>
            </view>
            <view class="field">
              <text class="field-label">简介</text>
              <input class="modal-input" placeholder="口味、做法等（选填）" v-model="form.description" />
            </view>
            <view class="field">
              <text class="field-label">图片</text>
              <view class="upload-row">
                <view class="upload-box" @click="chooseImage">
                  <SafeImage v-if="form.imageUrl" :src="form.imageUrl" mode="aspectFill" imgClass="image-preview" />
                  <view v-else class="upload-placeholder">
                    <AppIcon name="plus" :size="36" color="#B85C38" />
                    <text class="upload-tip">添加图片</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view class="form-section">
            <view class="section-cap-row">
              <text class="section-cap">扩展选项</text>
              <text class="section-link" @click="addOption">＋ 添加选项</text>
            </view>
            <text class="section-hint">用于辣度、份量等规格，可不填</text>

            <view v-if="vizOptions.length === 0" class="empty-ext">
              <text class="empty-text">暂无扩展选项</text>
            </view>

            <view v-for="(opt, oi) in vizOptions" :key="opt.__uid" class="opt-block">
              <view class="opt-top">
                <text class="opt-index">选项 {{ oi + 1 }}</text>
                <view class="opt-tools">
                  <text
                    class="tool-link"
                    :class="{ disabled: oi === 0 }"
                    @click="moveOptionSafe(oi, -1)"
                  >上移</text>
                  <text
                    class="tool-link"
                    :class="{ disabled: oi === vizOptions.length - 1 }"
                    @click="moveOptionSafe(oi, 1)"
                  >下移</text>
                  <text class="tool-link danger" @click="removeOption(oi)">删除</text>
                </view>
              </view>

              <input class="modal-input" placeholder="选项名称，如：辣度" v-model="opt.name" />

              <view class="opt-meta">
                <picker
                  :range="selectionTypeLabels"
                  :value="selectionTypes.indexOf(opt.selectionType)"
                  @change="e => changeType(oi, e.detail.value)"
                >
                  <view class="type-chip">{{ getSelectionTypeLabel(opt.selectionType) }} ▾</view>
                </picker>
                <view class="req-row" @click="toggleRequired(oi)">
                  <view class="req-check" :class="{ on: !!opt.required }"></view>
                  <text class="req-label">必填</text>
                </view>
              </view>

              <view v-if="opt.selectionType === 'multiple'" class="config-row">
                <view class="config-item">
                  <text class="config-label">最少</text>
                  <input class="config-input" type="number" placeholder="0" v-model.number="opt.min" />
                </view>
                <view class="config-item">
                  <text class="config-label">最多</text>
                  <input class="config-input" type="number" placeholder="不限" v-model.number="opt.max" />
                </view>
              </view>

              <view v-if="opt.selectionType === 'input'" class="field tight">
                <text class="field-label">占位提示</text>
                <input class="modal-input" placeholder="请输入..." v-model="opt.placeholder" />
              </view>

              <view v-if="opt.selectionType === 'number'" class="config-row">
                <view class="config-item">
                  <text class="config-label">单位</text>
                  <input class="config-input" placeholder="杯/份" v-model="opt.unit" />
                </view>
                <view class="config-item">
                  <text class="config-label">提示</text>
                  <input class="config-input" placeholder="请输入" v-model="opt.placeholder" />
                </view>
              </view>

              <view v-if="opt.selectionType === 'single' || opt.selectionType === 'multiple'" class="choices-wrap">
                <view class="choices-head">
                  <text class="choices-title">选择项</text>
                  <text class="section-link" @click="addChoice(oi)">＋ 添加</text>
                </view>
                <view v-if="!opt.choices || opt.choices.length === 0" class="choices-empty">
                  <text class="empty-text">还没有选择项</text>
                </view>
                <view v-for="(c, ci) in (opt.choices || [])" :key="c.__uid" class="choice-row">
                  <input class="choice-name" placeholder="名称" v-model="c.name" />
                  <input class="choice-price" type="digit" placeholder="加价" v-model.number="c.price" />
                  <view class="choice-del" @click="removeChoice(oi, ci)">
                    <AppIcon name="close" :size="22" color="#B33A2B" />
                  </view>
                </view>
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

    <view class="fab-button" @click="openCreate">
      <AppIcon name="plus" :size="40" color="#FFFFFF" />
    </view>
  </view>
  
</template>

<script>
import { dishApi, categoryApi, fileApi } from '@/utils/api.js'
import SafeImage from '@/components/SafeImage.vue'
import AppIcon from '@/components/AppIcon.vue'
import userManager from '@/utils/user.js'

export default {
  components: { SafeImage, AppIcon },
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
    moveOptionSafe(oi, delta) {
      this.moveOption(oi, delta)
    },
    toggleRequired(oi) {
      const opt = this.vizOptions[oi]
      if (!opt) return
      opt.required = !opt.required
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
        this.dishes = await dishApi.getList(null)
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
          dishApi.update({ id: dish.id, sort: dish.sort })
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
.container {
  min-height: 100%;
  padding: 24rpx 24rpx 180rpx;
  background: #F7F3EE;
  box-sizing: border-box;
}
.dish-list { display: flex; flex-direction: column; gap: 16rpx; }
.dish-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  border-radius: 20rpx;
  background: #FFFFFF;
  box-shadow: 0 4rpx 20rpx rgba(42, 36, 32, 0.05);
  padding: 20rpx;
}
.sort-handle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40rpx;
  height: 40rpx;
}
.dragging { opacity: 0.55; }
.drag-over { border: 2rpx dashed #B85C38; }
.dish-image {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  background: #F7F3EE;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
:deep(.dish-image) { width: 80rpx; height: 80rpx; border-radius: 12rpx; }
.info { flex: 1; display: flex; flex-direction: column; gap: 6rpx; min-width: 0; }
.name { font-size: 28rpx; color: #2A2420; font-weight: 600; }
.price { font-size: 26rpx; color: #B85C38; font-weight: 700; }
.status { font-size: 22rpx; color: #9A9086; }
.status.on { color: #5C6B5A; }
.actions { display: flex; gap: 8rpx; }
.action-btn {
  padding: 8rpx 18rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
  border: none;
  min-width: 80rpx;
  margin: 0;
  line-height: 1.4;
}
.action-btn::after { border: none; }
.toggle-btn { background: #F7F3EE; color: #6B6158; }
.toggle-btn.btn-on { background: #5C6B5A; color: #fff; }
.edit-btn { background: #B85C38; color: #fff; }
.modal-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  padding-top: 200rpx;
  background: rgba(42, 36, 32, 0.48);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  z-index: 2000;
  box-sizing: border-box;
}
.modal-card {
  width: 100%;
  max-height: 100%;
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
  border-radius: 28rpx 28rpx 0 0;
  overflow: hidden;
  box-sizing: border-box;
}
.modal-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24rpx;
  padding: 36rpx 36rpx 28rpx;
  border-bottom: 1rpx solid #E8E0D6;
  flex-shrink: 0;
}
.modal-head-text {
  flex: 1;
  min-width: 0;
}
.modal-title {
  display: block;
  font-size: 34rpx;
  font-weight: 600;
  color: #2A2420;
  line-height: 1.3;
}
.modal-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #9A9086;
}
.modal-close {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #F7F3EE;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.modal-body {
  flex: 1;
  height: 0;
  padding: 28rpx 36rpx 40rpx;
  box-sizing: border-box;
}
.modal-actions {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 36rpx calc(28rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #E8E0D6;
  flex-shrink: 0;
  background: #FFFFFF;
}
.form-section {
  margin-bottom: 40rpx;
}
.form-section:last-child {
  margin-bottom: 0;
}
.section-cap {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: #2A2420;
  margin-bottom: 20rpx;
  letter-spacing: 1rpx;
}
.section-cap-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8rpx;
}
.section-cap-row .section-cap {
  margin-bottom: 0;
}
.section-link {
  font-size: 26rpx;
  color: #B85C38;
  font-weight: 500;
}
.section-hint {
  display: block;
  font-size: 22rpx;
  color: #9A9086;
  margin-bottom: 20rpx;
}
.field {
  margin-bottom: 24rpx;
}
.field.tight {
  margin-top: 16rpx;
  margin-bottom: 0;
}
.field.half {
  flex: 1;
  margin-bottom: 0;
}
.field-row {
  display: flex;
  gap: 20rpx;
  margin-bottom: 24rpx;
}
.field-label {
  display: block;
  font-size: 24rpx;
  color: #9A9086;
  margin-bottom: 12rpx;
}
.modal-input {
  background: #F7F3EE;
  border: 2rpx solid #E8E0D6;
  border-radius: 16rpx;
  height: 88rpx;
  min-height: 88rpx;
  line-height: 88rpx;
  padding: 0 28rpx;
  font-size: 28rpx;
  color: #2A2420;
  width: 100%;
  box-sizing: border-box;
}
.modal-input:focus {
  border-color: #B85C38;
  background: #FFFFFF;
}
.upload-row {
  display: flex;
  align-items: center;
}
.upload-box {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  background: #F7F3EE;
  border: 2rpx dashed #D4C9BC;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}
.upload-tip {
  font-size: 22rpx;
  color: #B85C38;
}
:deep(.image-preview) {
  width: 160rpx;
  height: 160rpx;
}
.picker-text {
  height: 88rpx;
  line-height: 88rpx;
  padding: 0 24rpx;
  background: #F7F3EE;
  border: 2rpx solid #E8E0D6;
  border-radius: 16rpx;
  color: #B85C38;
  font-size: 26rpx;
  box-sizing: border-box;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.empty-ext {
  padding: 36rpx 20rpx;
  text-align: center;
  background: #F7F3EE;
  border-radius: 16rpx;
}
.empty-text {
  font-size: 26rpx;
  color: #9A9086;
}
.opt-block {
  background: #F7F3EE;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 20rpx;
}
.opt-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.opt-index {
  font-size: 26rpx;
  font-weight: 600;
  color: #2A2420;
}
.opt-tools {
  display: flex;
  align-items: center;
  gap: 24rpx;
}
.tool-link {
  font-size: 24rpx;
  color: #6B6158;
}
.tool-link.danger {
  color: #B33A2B;
}
.tool-link.disabled {
  color: #D4C9BC;
}
.opt-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
}
.type-chip {
  padding: 12rpx 20rpx;
  background: #FFFFFF;
  border-radius: 999rpx;
  font-size: 24rpx;
  color: #B85C38;
  border: 2rpx solid rgba(184, 92, 56, 0.25);
}
.req-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 8rpx 0;
}
.req-check {
  width: 36rpx;
  height: 36rpx;
  border-radius: 8rpx;
  border: 2rpx solid #D4C9BC;
  background: #FFFFFF;
  box-sizing: border-box;
}
.req-check.on {
  background: #B85C38;
  border-color: #B85C38;
}
.req-label {
  font-size: 26rpx;
  color: #6B6158;
}
.config-row {
  display: flex;
  gap: 16rpx;
  margin-top: 16rpx;
}
.config-item { flex: 1; }
.config-label {
  display: block;
  font-size: 22rpx;
  color: #9A9086;
  margin-bottom: 8rpx;
}
.config-input {
  background: #FFFFFF;
  border: 2rpx solid #E8E0D6;
  border-radius: 12rpx;
  height: 72rpx;
  min-height: 72rpx;
  line-height: 72rpx;
  padding: 0 16rpx;
  font-size: 26rpx;
  width: 100%;
  box-sizing: border-box;
}
.choices-wrap {
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #E8E0D6;
}
.choices-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}
.choices-title {
  font-size: 24rpx;
  font-weight: 600;
  color: #6B6158;
}
.choices-empty {
  padding: 16rpx 0;
}
.choice-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}
.choice-name {
  flex: 1;
  background: #FFFFFF;
  border: 2rpx solid #E8E0D6;
  border-radius: 12rpx;
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 16rpx;
  font-size: 26rpx;
  box-sizing: border-box;
}
.choice-price {
  width: 140rpx;
  background: #FFFFFF;
  border: 2rpx solid #E8E0D6;
  border-radius: 12rpx;
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 12rpx;
  font-size: 26rpx;
  box-sizing: border-box;
  text-align: center;
}
.choice-del {
  width: 56rpx;
  height: 56rpx;
  border-radius: 12rpx;
  background: rgba(179, 58, 43, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.modal-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  padding: 0;
  border-radius: 16rpx;
  font-size: 28rpx;
  border: none;
  margin: 0;
  font-weight: 600;
}
.modal-btn::after { border: none; }
.cancel-btn { background: #F7F3EE; color: #6B6158; }
.save-btn { background: #B85C38; color: #fff; }
.fab-button {
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
  z-index: 1500;
}
</style>
