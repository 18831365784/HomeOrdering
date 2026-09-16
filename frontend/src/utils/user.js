/**
 * 用户信息管理
 */
class UserManager {
  constructor() {
    this.USER_KEY = 'user_info'
    this.TOKEN_KEY = 'auth_token'
  }

  saveUserInfo(userInfo) {
    try {
      uni.setStorageSync(this.USER_KEY, { ...userInfo })
      return true
    } catch (e) {
      console.error('保存用户信息失败:', e)
      return false
    }
  }

  getUserInfo() {
    try {
      return uni.getStorageSync(this.USER_KEY) || null
    } catch (e) {
      console.error('获取用户信息失败:', e)
      return null
    }
  }

  saveToken(token) {
    try {
      uni.setStorageSync(this.TOKEN_KEY, token)
      return true
    } catch (e) {
      console.error('保存令牌失败:', e)
      return false
    }
  }

  getToken() {
    try {
      return uni.getStorageSync(this.TOKEN_KEY) || null
    } catch (e) {
      console.error('获取令牌失败:', e)
      return null
    }
  }

  getUuid() {
    const userInfo = this.getUserInfo()
    return userInfo ? userInfo.uuid : null
  }

  isLoggedIn() {
    return !!(this.getToken() && this.getUuid())
  }

  /** 是否家庭管理员：以服务端下发的 isAdmin 为准，不以 role 为权威 */
  isAdmin() {
    const userInfo = this.getUserInfo()
    return !!(userInfo && userInfo.isAdmin)
  }

  clearUserInfo() {
    try {
      uni.removeStorageSync(this.USER_KEY)
      uni.removeStorageSync(this.TOKEN_KEY)
      return true
    } catch (e) {
      console.error('清除用户信息失败:', e)
      return false
    }
  }
}

export default new UserManager()
