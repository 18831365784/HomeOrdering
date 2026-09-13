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

// 创建家庭
export const createFamily = (uuid, name) => {
  return request('/family', {
    method: 'POST',
    data: { uuid, name }
  })
}

// 加入家庭
export const joinFamily = (uuid, inviteCode) => {
  return request('/family/join', {
    method: 'POST',
    data: { uuid, inviteCode }
  })
}

// 获取家庭信息 - uuid 作为 URL 参数
export const getFamilyInfo = (uuid) => {
  return request('/family/info?uuid=' + encodeURIComponent(uuid), {
    method: 'GET'
  })
}

// 获取家庭成员列表 - uuid 作为 URL 参数
export const getFamilyMembers = (uuid) => {
  return request('/family/members?uuid=' + encodeURIComponent(uuid), {
    method: 'GET'
  })
}

// 重新生成邀请码 - uuid 作为 URL 参数
export const regenerateInviteCode = (uuid) => {
  return request('/family/regenerate-code?uuid=' + encodeURIComponent(uuid), {
    method: 'POST'
  })
}

// 判断是否是家庭管理员 - uuid 作为 URL 参数
export const isFamilyAdmin = (uuid) => {
  return request('/family/is-admin?uuid=' + encodeURIComponent(uuid), {
    method: 'GET'
  })
}

export default {
  createFamily,
  joinFamily,
  getFamilyInfo,
  getFamilyMembers,
  regenerateInviteCode,
  isFamilyAdmin
}