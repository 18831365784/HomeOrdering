// API基础配置
// 开发环境使用 localhost，生产环境使用 ngrok 地址
const BASE_URL = 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api'

// 请求封装
const request = (url, options = {}) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'ngrok-skip-browser-warning': 'true',
        ...options.header
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data.data)
        } else {
          uni.showToast({
            title: res.data.message || '请求失败',
            icon: 'none'
          })
          reject(res.data)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络请求失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

// 文件上传封装
const uploadFile = (filePath) => {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + '/file/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'ngrok-skip-browser-warning': 'true'
      },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          resolve(data.data)
        } else {
          uni.showToast({
            title: data.message || '上传失败',
            icon: 'none'
          })
          reject(data)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

// 上传分类图标
const uploadIcon = (filePath) => {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + '/file/upload/icon',
      filePath: filePath,
      name: 'file',
      header: { 'ngrok-skip-browser-warning': 'true' },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          resolve(data.data)
        } else {
          uni.showToast({ title: data.message || '上传失败', icon: 'none' })
          reject(data)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 菜品API
export const dishApi = {
  getList(status) {
    return request('/dish/list', {
      method: 'GET',
      data: { status }
    })
  },
  getDetail(id) {
    return request(`/dish/${id}`)
  },
  add(data) {
    return request('/dish', {
      method: 'POST',
      data
    })
  },
  update(data) {
    return request('/dish', {
      method: 'PUT',
      data
    })
  },
  delete(id) {
    return request(`/dish/${id}`, {
      method: 'DELETE'
    })
  }
}

// 订单API
export const orderApi = {
  create(data) {
    return request('/order', {
      method: 'POST',
      data
    })
  },
  getList(status) {
    return request('/order/list', {
      method: 'GET',
      data: { status }
    })
  },
  getDetail(id) {
    return request(`/order/${id}`)
  },
  updateStatus(id, status) {
    return request(`/order/${id}/status`, {
      method: 'PUT',
      data: { status }
    })
  },
  delete(id) {
    return request(`/order/${id}`, {
      method: 'DELETE'
    })
  }
}

// 文件API
export const fileApi = {
  upload(filePath) {
    return uploadFile(filePath)
  },
  uploadIcon(filePath) {
    return uploadIcon(filePath)
  }
}

// 用户API
export const userApi = {
  checkAdmin(uuid) {
    return request('/user/checkAdmin', {
      method: 'GET',
      data: { uuid }
    })
  }
}

// 认证API
export const authApi = {
  wxLogin(data) {
    return request('/auth/login', {
      method: 'POST',
      data
    })
  },
  getCurrentUser() {
    return request('/auth/me', {
      method: 'GET'
    })
  },
  logout() {
    return request('/auth/logout', {
      method: 'POST'
    })
  }
}

// 分类API
export const categoryApi = {
  getList(status) {
    return request('/category/list', {
      method: 'GET',
      data: { status }
    })
  },
  add(data) { return request('/category', { method: 'POST', data }) },
  update(data) { return request('/category', { method: 'PUT', data }) },
  delete(id) { return request(`/category/${id}`, { method: 'DELETE' }) }
}

export default {
  dishApi,
  orderApi,
  fileApi,
  userApi,
  authApi,
  categoryApi
}