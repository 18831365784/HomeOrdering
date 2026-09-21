/**
 * 前端唯一 API 配置源：VITE_API_BASE_URL（见 frontend/.env.development / .env.production）
 */
const BASE_URL = import.meta.env.VITE_API_BASE_URL

/** 将 /uploads/... 或旧绝对上传地址拼成当前可访问的绝对 URL；外链原样返回 */
export function resolveMediaUrl(url) {
  if (!url || typeof url !== 'string') return url || ''
  const value = url.trim()
  const marker = '/uploads/'
  const idx = value.indexOf(marker)
  if (idx >= 0) {
    const base = (typeof BASE_URL === 'string' ? BASE_URL : '').replace(/\/$/, '')
    return base + value.substring(idx)
  }
  return value
}

function buildHeaders(extra = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...extra
  }
  try {
    const token = uni.getStorageSync('auth_token')
    if (token) {
      headers.Authorization = token
    }
  } catch (e) {
    // ignore
  }
  if (typeof BASE_URL === 'string' && BASE_URL.includes('ngrok')) {
    headers['ngrok-skip-browser-warning'] = 'true'
  }
  return headers
}

function handleUnauthorized() {
  try {
    uni.removeStorageSync('auth_token')
    uni.removeStorageSync('user_info')
  } catch (e) {
    // ignore
  }
  uni.showToast({ title: '请重新登录', icon: 'none' })
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/login/index' })
  }, 400)
}

const request = (url, options = {}) => {
  return new Promise((resolve, reject) => {
    const method = (options.method || 'GET').toUpperCase()
    let data = options.data || {}
    // 小程序 POST/PUT JSON 体显式序列化，避免字段被错绑
    if ((method === 'POST' || method === 'PUT' || method === 'PATCH') && data && typeof data === 'object') {
      data = JSON.stringify(data)
    }
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      timeout: 20000,
      header: buildHeaders(options.header || {}),
      success: (res) => {
        if (res.statusCode === 401) {
          handleUnauthorized()
          reject(res.data || { message: '未登录' })
          return
        }
        if (res.data && res.data.code === 200) {
          resolve(res.data.data)
        } else {
          uni.showToast({
            title: (res.data && res.data.message) || '请求失败',
            icon: 'none'
          })
          reject(res.data)
        }
      },
      fail: (err) => {
        const msg = (err && (err.errMsg || err.message)) || ''
        uni.showToast({
          title: /timeout/i.test(msg) ? '请求超时，请确认后端已启动' : '网络请求失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

const uploadFile = (filePath, path = '/file/upload') => {
  return new Promise((resolve, reject) => {
    const header = {}
    try {
      const token = uni.getStorageSync('auth_token')
      if (token) header.Authorization = token
    } catch (e) {}
    if (typeof BASE_URL === 'string' && BASE_URL.includes('ngrok')) {
      header['ngrok-skip-browser-warning'] = 'true'
    }
    uni.uploadFile({
      url: BASE_URL + path,
      filePath,
      name: 'file',
      timeout: 20000,
      header,
      success: (res) => {
        if (res.statusCode === 401) {
          handleUnauthorized()
          reject({ message: '未登录' })
          return
        }
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

export const dishApi = {
  getList(status) {
    const data = {}
    if (status !== null && status !== undefined) data.status = status
    return request('/dish/list', { method: 'GET', data })
  },
  getDetail(id) {
    return request(`/dish/${id}`)
  },
  add(data) {
    return request('/dish', { method: 'POST', data })
  },
  update(data) {
    return request('/dish', { method: 'PUT', data })
  },
  delete(id) {
    return request(`/dish/${id}`, { method: 'DELETE' })
  }
}

export const orderApi = {
  create(data) {
    return request('/order', { method: 'POST', data })
  },
  getMyOrders() {
    return request('/order/list/my', { method: 'GET' })
  },
  getMyMakingOrders() {
    return request('/order/list/making', { method: 'GET' })
  },
  getList(status) {
    const data = {}
    if (status !== null && status !== undefined) data.status = status
    return request('/order/list', { method: 'GET', data })
  },
  getDetail(id) {
    return request(`/order/${id}`)
  },
  acceptOrder(id) {
    return request(`/order/${id}/accept`, { method: 'PUT' })
  },
  rejectOrder(id) {
    return request(`/order/${id}/reject`, { method: 'PUT' })
  },
  finishOrder(id) {
    return request(`/order/${id}/finish`, { method: 'PUT' })
  },
  cancelOrder(id) {
    return request(`/order/${id}/cancel`, { method: 'PUT' })
  }
}

export const fileApi = {
  upload(filePath) {
    return uploadFile(filePath, '/file/upload')
  },
  uploadIcon(filePath) {
    return uploadFile(filePath, '/file/upload/icon')
  }
}

export const userApi = {
  getInfo(uuid) {
    const data = {}
    if (uuid) data.uuid = uuid
    return request('/user/info', { method: 'GET', data })
  },
  updateProfile(data) {
    return request('/user/profile', { method: 'PUT', data })
  },
  updateBalance(uuid, balance) {
    return request('/user/balance', {
      method: 'PUT',
      data: { uuid, balance }
    })
  }
}

export const authApi = {
  wxLogin(data) {
    return request('/auth/login', { method: 'POST', data })
  },
  getCurrentUser() {
    return request('/auth/me', { method: 'GET' })
  },
  logout() {
    return request('/auth/logout', { method: 'POST' })
  }
}

export const categoryApi = {
  getList(status) {
    const data = {}
    if (status !== null && status !== undefined) data.status = status
    return request('/category/list', { method: 'GET', data })
  },
  add(data) {
    return request('/category', { method: 'POST', data })
  },
  update(data) {
    return request('/category', { method: 'PUT', data })
  },
  delete(id) {
    return request(`/category/${id}`, { method: 'DELETE' })
  }
}

export const familyApi = {
  createFamily(name) {
    return request('/family', { method: 'POST', data: { name } })
  },
  joinFamily(inviteCode) {
    return request('/family/join', { method: 'POST', data: { inviteCode } })
  },
  getFamilyInfo() {
    return request('/family/info', { method: 'GET' })
  },
  getFamilyMembers() {
    return request('/family/members', { method: 'GET' })
  },
  regenerateInviteCode() {
    return request('/family/regenerate-code', { method: 'POST' })
  },
  updateFamilyName(name) {
    return request('/family/name', { method: 'PUT', data: { name } })
  },
  isFamilyAdmin() {
    return request('/family/is-admin', { method: 'GET' })
  }
}

export function getApiBaseUrl() {
  return BASE_URL
}

export default {
  request,
  getApiBaseUrl,
  resolveMediaUrl,
  dishApi,
  orderApi,
  fileApi,
  userApi,
  authApi,
  categoryApi,
  familyApi
}
