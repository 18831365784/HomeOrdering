// API基础配置
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
        'ngrok-skip-browser-warning': 'true',  // 跳过 ngrok 浏览器警告页
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
        'ngrok-skip-browser-warning': 'true'  // 跳过 ngrok 浏览器警告页
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

// 上传分类图标到 uploads/icon
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
  // 获取菜品列表
  getList(status) {
    if (status !== null && status !== undefined) {
      return request('/dish/list', {
        method: 'GET',
        data: { status }
      })
    } else {
      return request('/dish/list', {
        method: 'GET'
      })
    }
  },
  
  // 获取菜品详情
  getDetail(id) {
    return request(`/dish/${id}`)
  },
  
  // 添加菜品
  add(data) {
    return request('/dish', {
      method: 'POST',
      data
    })
  },
  
  // 更新菜品
  update(data) {
    return request('/dish', {
      method: 'PUT',
      data
    })
  },
  
  // 删除菜品
  delete(id) {
    return request(`/dish/${id}`, {
      method: 'DELETE'
    })
  }
}

// 订单API
export const orderApi = {
  // 创建订单
  create(data) {
    return request('/order', {
      method: 'POST',
      data
    })
  },
  
  // 获取订单列表
  getList(status) {
    const params = {}
    // 只有status不为null时才传递
    if (status !== null && status !== undefined) {
      params.status = status
    }
    return request('/order/list', {
      method: 'GET',
      data: params
    })
  },
  
  // 获取订单详情
  getDetail(id) {
    return request(`/order/${id}`)
  },
  
  // 更新订单状态
  updateStatus(id, status) {
    return request(`/order/${id}/status`, {
      method: 'PUT',
      data: { status }
    })
  },
  
  // 删除订单
  delete(id) {
    return request(`/order/${id}`, {
      method: 'DELETE'
    })
  }
}

// 文件API
export const fileApi = {
  // 上传文件
  upload(filePath) {
    return uploadFile(filePath)
  },
  uploadIcon(filePath) {
    return uploadIcon(filePath)
  }
}

// 用户API
export const userApi = {
  // 微信登录
  wxLogin(data) {
    return request('/user/wxLogin', {
      method: 'POST',
      data
    })
  },
  
  // 检查是否为管理员
  checkAdmin(uuid) {
    return request('/user/checkAdmin', {
      method: 'GET',
      data: { uuid }
    })
  }
}

// 分类API
export const categoryApi = {
  // 获取分类列表
  getList(status) {
    const params = {}
    if (status !== null && status !== undefined) {
      params.status = status
    }
    return request('/category/list', {
      method: 'GET',
      data: params
    })
  },
  // 新增/更新/删除（后台维护时可用）
  add(data) { return request('/category', { method: 'POST', data }) },
  update(data) { return request('/category', { method: 'PUT', data }) },
  delete(id) { return request(`/category/${id}`, { method: 'DELETE' }) }
}

export default {
  dishApi,
  orderApi,
  fileApi,
  userApi,
  categoryApi
}
