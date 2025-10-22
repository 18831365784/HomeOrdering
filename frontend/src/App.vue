<script>
import { userApi } from '@/utils/api.js'
import userManager from '@/utils/user.js'

export default {
  onLaunch: function() {
    console.log('App Launch')
    this.checkLogin()
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  },
  
  methods: {
    // 检查登录状态
    checkLogin() {
      // 每次都重新登录，以获取最新的用户信息（包括 role）
      uni.login({
        provider: 'weixin',
        success: (loginRes) => {
          console.log('微信登录返回:', loginRes)
          
          if (loginRes.code) {
            // 调用后端登录接口
            userApi.wxLogin({
              code: loginRes.code,
              nickname: '用户',
              avatarUrl: ''
            }).then(userInfo => {
              // 保存用户信息
              userManager.saveUserInfo(userInfo)
              console.log('登录成功，用户信息:', userInfo)
              console.log('是否为管理员:', userInfo.role === 1)
              
              // 触发全局事件，通知页面刷新权限状态
              uni.$emit('userInfoUpdated', userInfo)
            }).catch(error => {
              console.error('后端登录失败:', error)
            })
          } else {
            console.error('获取code失败:', loginRes)
          }
        },
        fail: (error) => {
          console.error('微信登录失败:', error)
        }
      })
    }
  }
}
</script>

<style>
@import './app.css';
</style>
