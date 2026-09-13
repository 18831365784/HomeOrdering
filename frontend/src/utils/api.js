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
  // 获取菜品列表（需要传uuid获取当前家庭）
  getList(status, uuid) {
    const data = {}
    if (uuid) data.uuid = uuid
    if (status !== null && status !== undefined) data.status = status
    return request('/dish/list', {
      method: 'GET',
      data
    })
  },

  // 获取菜品详情
  getDetail(id) {
    return request(`/dish/${id}`)
  },

  // 添加菜品
  add(data, uuid) {
    return request(`/dish?uuid=${encodeURIComponent(uuid)}`, {
      method: 'POST',
      data
    })
  },

  // 更新菜品
  update(data, uuid) {
    return request(`/dish?uuid=${encodeURIComponent(uuid)}`, {
      method: 'PUT',
      data
    })
  },

  // 删除菜品
  delete(id, uuid) {
    return request(`/dish/${id}?uuid=${encodeURIComponent(uuid)}`, {
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

  // 获取我的订单（我下的单）
  getMyOrders(uuid) {
    return request('/order/list/my', {
      method: 'GET',
      data: { uuid }
    })
  },

  // 获取我的制作（我作为制作者的订单）
  getMyMakingOrders(uuid) {
    return request('/order/list/making', {
      method: 'GET',
      data: { uuid }
    })
  },

  // 获取订单列表（全部）
  getList(status) {
    const params = {}
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

  // 制作者接单
  acceptOrder(id, makerUuid) {
    return request(`/order/${id}/accept?makerUuid=${encodeURIComponent(makerUuid)}`, {
      method: 'PUT'
    })
  },

  // 制作者完成订单
  finishOrder(id, makerUuid) {
    return request(`/order/${id}/finish?makerUuid=${encodeURIComponent(makerUuid)}`, {
      method: 'PUT'
    })
  },

  // 取消订单
  cancelOrder(id, customerUuid) {
    return request(`/order/${id}/cancel?customerUuid=${encodeURIComponent(customerUuid)}`, {
      method: 'PUT'
    })
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
  // 获取用户信息
  getInfo(uuid) {
    return request('/user/info', {
      method: 'GET',
      data: { uuid }
    })
  },
  // 检查是否为管理员
  checkAdmin(uuid) {
    return request('/user/checkAdmin', {
      method: 'GET',
      data: { uuid }
    })
  },
  // 更新个人资料
  updateProfile(data) {
    return request('/user/profile', {
      method: 'PUT',
      data
    })
  },
  // 更新余额（管理员）
  updateBalance(uuid, balance) {
    return request('/user/balance', {
      method: 'PUT',
      data: { uuid, balance }
    })
  }
}

// 认证API
export const authApi = {
  // 微信登录
  wxLogin(data) {
    return request('/auth/login', {
      method: 'POST',
      data
    })
  },

  // 获取当前用户
  getCurrentUser() {
    return request('/auth/me', {
      method: 'GET'
    })
  },

  // 退出登录
  logout() {
    return request('/auth/logout', {
      method: 'POST'
    })
  }
}

// 分类API
export const categoryApi = {
  // 获取分类列表（需要传uuid获取当前家庭）
  getList(status, uuid) {
    const params = {}
    if (uuid) params.uuid = uuid
    if (status !== null && status !== undefined) params.status = status
    return request('/category/list', {
      method: 'GET',
      data: params
    })
  },
  // 新增/更新/删除（后台维护时可用）
  add(data, uuid) { return request(`/category?uuid=${encodeURIComponent(uuid)}`, { method: 'POST', data }) },
  update(data, uuid) { return request(`/category?uuid=${encodeURIComponent(uuid)}`, { method: 'PUT', data }) },
  delete(id, uuid) { return request(`/category/${id}?uuid=${encodeURIComponent(uuid)}`, { method: 'DELETE' }) }
}

export default {
  request,
  dishApi,
  orderApi,
  fileApi,
  userApi,
  authApi,
  categoryApi
}
