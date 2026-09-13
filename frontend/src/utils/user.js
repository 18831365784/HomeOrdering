/**
 * 用户信息管理
 */
class UserManager {
  constructor() {
    this.USER_KEY = 'user_info'
    this.TOKEN_KEY = 'auth_token'
  }

  /**
   * 保存用户信息
   */
  saveUserInfo(userInfo) {
    try {
      // 确保 familyId 被保存
      const infoToSave = {
        ...userInfo,
        familyId: userInfo.familyId
      }
      uni.setStorageSync(this.USER_KEY, infoToSave)
      return true
    } catch (e) {
      console.error('保存用户信息失败:', e)
      return false
    }
  }

  /**
   * 获取用户信息
   */
  getUserInfo() {
    try {
      return uni.getStorageSync(this.USER_KEY) || null
    } catch (e) {
      console.error('获取用户信息失败:', e)
      return null
    }
  }

  /**
   * 保存登录令牌
   */
  saveToken(token) {
    try {
      uni.setStorageSync(this.TOKEN_KEY, token)
      return true
    } catch (e) {
      console.error('保存令牌失败:', e)
      return false
    }
  }

  /**
   * 获取登录令牌
   */
  getToken() {
    try {
      return uni.getStorageSync(this.TOKEN_KEY) || null
    } catch (e) {
      console.error('获取令牌失败:', e)
      return null
    }
  }

  /**
   * 获取UUID
   */
  getUuid() {
    const userInfo = this.getUserInfo()
    return userInfo ? userInfo.uuid : null
  }

  /**
   * 检查是否已登录
   */
  isLoggedIn() {
    const userInfo = this.getUserInfo()
    return userInfo && userInfo.openid
  }

  /**
   * 检查是否为管理员
   */
  isAdmin() {
    const userInfo = this.getUserInfo()
    return userInfo && userInfo.role === 1
  }

  /**
   * 清除用户信息
   */
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