<template>
  <view class="container">
    <view class="form-container">
      <!-- 图片上传 -->
      <view class="form-item card">
        <text class="form-label">菜品图片</text>
        <view class="image-upload">
          <view v-if="imageUrl" class="image-preview">
            <image :src="imageUrl" mode="aspectFill" />
            <view class="image-delete" @click="deleteImage">
              <text>×</text>
            </view>
          </view>
          <view v-else class="upload-btn" @click="chooseImage">
            <text class="upload-icon">+</text>
            <text class="upload-text">选择图片</text>
          </view>
        </view>
      </view>
      
      <!-- 菜品名称 -->
      <view class="form-item card">
        <text class="form-label">菜品名称 <text class="required">*</text></text>
        <input 
          class="form-input" 
          v-model="formData.name" 
          placeholder="请输入菜品名称"
          maxlength="50"
        />
      </view>
      
      <!-- 菜品价格 -->
      <view class="form-item card">
        <text class="form-label">菜品价格 <text class="required">*</text></text>
        <view class="price-input">
          <text class="currency">¥</text>
          <input 
            class="form-input" 
            v-model="formData.price" 
            type="digit"
            placeholder="请输入价格"
          />
        </view>
      </view>
      
      <!-- 菜品分类 -->
      <view class="form-item card">
        <text class="form-label">菜品分类 <text class="required">*</text></text>
        <view class="category-list">
          <view 
            v-for="cat in categories" 
            :key="cat"
            class="category-item"
            :class="{ 'active': formData.category === cat }"
            @click="selectCategory(cat)"
          >
            <text>{{ cat }}</text>
          </view>
        </view>
      </view>
      
      <!-- 菜品简介 -->
      <view class="form-item card">
        <text class="form-label">菜品简介</text>
        <textarea 
          class="form-textarea" 
          v-model="formData.description" 
          placeholder="请输入菜品简介"
          maxlength="200"
        />
      </view>
      
      <!-- 提交按钮 -->
      <view class="submit-buttons">
        <button class="btn btn-primary primary-bg" @click="submitForm">
          添加菜品
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { dishApi, fileApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'

export default {
  data() {
    return {
      imageUrl: '',
      categories: ['肉类', '蔬菜', '主食', '凉菜', '汤'],
      formData: {
        name: '',
        price: '',
        category: '',
        description: '',
        imageUrl: '',
        status: 1
      }
    }
  },
  
  onLoad() {
    // 检查是否有权限
    if (!userManager.isAdmin()) {
      uni.showModal({
        title: '提示',
        content: '您没有权限访问此页面',
        showCancel: false,
        success: () => {
          uni.switchTab({
            url: '/pages/index/index'
          })
        }
      })
    }
  },
  
  methods: {
    // 选择分类
    selectCategory(category) {
      this.formData.category = category
    },
    
    // 选择图片
    chooseImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          const tempFilePath = res.tempFilePaths[0]
          await this.uploadImage(tempFilePath)
        }
      })
    },
    
    // 上传图片
    async uploadImage(filePath) {
      try {
        uni.showLoading({ title: '上传中...' })
        const imageUrl = await fileApi.upload(filePath)
        this.imageUrl = imageUrl
        this.formData.imageUrl = imageUrl
        uni.showToast({
          title: '上传成功',
          icon: 'success'
        })
      } catch (error) {
        console.error('上传图片失败:', error)
      } finally {
        uni.hideLoading()
      }
    },
    
    // 删除图片
    deleteImage() {
      uni.showModal({
        title: '提示',
        content: '确定要删除这张图片吗？',
        success: (res) => {
          if (res.confirm) {
            this.imageUrl = ''
            this.formData.imageUrl = ''
          }
        }
      })
    },
    
    // 验证表单
    validateForm() {
      if (!this.formData.name) {
        uni.showToast({
          title: '请输入菜品名称',
          icon: 'none'
        })
        return false
      }
      
      if (!this.formData.price) {
        uni.showToast({
          title: '请输入菜品价格',
          icon: 'none'
        })
        return false
      }
      
      const price = parseFloat(this.formData.price)
      if (isNaN(price) || price <= 0) {
        uni.showToast({
          title: '请输入有效的价格',
          icon: 'none'
        })
        return false
      }
      
      if (!this.formData.category) {
        uni.showToast({
          title: '请选择菜品分类',
          icon: 'none'
        })
        return false
      }
      
      return true
    },
    
    // 提交表单
    async submitForm() {
      if (!this.validateForm()) {
        return
      }
      
      try {
        uni.showLoading({ title: '提交中...' })
        
        // 提交数据
        const submitData = {
          ...this.formData,
          price: parseFloat(this.formData.price)
        }
        
        await dishApi.add(submitData)
        
        uni.hideLoading()
        
        uni.showModal({
          title: '成功',
          content: '菜品添加成功！',
          showCancel: false,
          success: () => {
            // 重置表单
            this.resetForm()
            
            // 跳转到首页
            uni.switchTab({
              url: '/pages/index/index'
            })
          }
        })
      } catch (error) {
        uni.hideLoading()
        console.error('添加菜品失败:', error)
      }
    },
    
    // 重置表单
    resetForm() {
      this.imageUrl = ''
      this.formData = {
        name: '',
        price: '',
        category: '',
        description: '',
        imageUrl: '',
        status: 1
      }
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  padding: 20rpx;
  padding-bottom: 160rpx;
}

.form-container {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333333;
  font-weight: bold;
}

.required {
  color: #FF6B6B;
}

.image-upload {
  width: 200rpx;
  height: 200rpx;
}

.image-preview {
  width: 100%;
  height: 100%;
  position: relative;
  border-radius: 12rpx;
  overflow: hidden;
}

.image-preview image {
  width: 100%;
  height: 100%;
}

.image-delete {
  position: absolute;
  top: 0;
  right: 0;
  width: 50rpx;
  height: 50rpx;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 40rpx;
  font-weight: bold;
}

.upload-btn {
  width: 100%;
  height: 100%;
  border: 2rpx dashed #e0e0e0;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  background-color: #fafafa;
}

.upload-icon {
  font-size: 60rpx;
  color: #999999;
}

.upload-text {
  font-size: 24rpx;
  color: #999999;
}

.form-input {
  background-color: #f8f8f8;
  border-radius: 8rpx;
  padding: 24rpx;
  font-size: 28rpx;
}

.price-input {
  display: flex;
  align-items: center;
  background-color: #f8f8f8;
  border-radius: 8rpx;
  padding: 24rpx;
}

.currency {
  font-size: 28rpx;
  color: #FF6B6B;
  font-weight: bold;
  margin-right: 12rpx;
}

.form-textarea {
  background-color: #f8f8f8;
  border-radius: 8rpx;
  padding: 24rpx;
  font-size: 28rpx;
  min-height: 200rpx;
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.category-item {
  flex: 0 0 calc(33.333% - 14rpx);
  padding: 24rpx;
  background-color: #f8f8f8;
  border-radius: 8rpx;
  text-align: center;
  font-size: 28rpx;
  color: #666666;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.category-item.active {
  background-color: #fff0f0;
  color: #FF6B6B;
  border-color: #FF6B6B;
  font-weight: bold;
}

.submit-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 20rpx;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.submit-buttons .btn {
  width: 100%;
}
</style>
