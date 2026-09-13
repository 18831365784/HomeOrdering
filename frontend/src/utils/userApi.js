// 用户相关 API
import request from './index.js'

// 获取用户信息
export const getUserInfo = (uuid) => {
  return request('/user/info', {
    method: 'GET',
    data: { uuid }
  })
}

// 更新用户资料（昵称、头像）
export const updateProfile = (data) => {
  return request('/user/profile', {
    method: 'PUT',
    data
  })
}

// 更新余额（管理员）
export const updateBalance = (uuid, balance) => {
  return request('/user/balance', {
    method: 'PUT',
    data: { uuid, balance }
  })
}

// 检查是否为管理员
export const checkAdmin = (uuid) => {
  return request('/user/checkAdmin', {
    method: 'GET',
    data: { uuid }
  })
}

export default {
  getUserInfo,
  updateProfile,
  updateBalance,
  checkAdmin
}
