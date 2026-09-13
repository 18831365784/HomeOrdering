<script>
import userManager from '@/utils/user.js'
import familyApi from '@/utils/familyApi.js'

export default {
  onLaunch: function() {
    console.log('App Launch - 检查登录状态和家庭状态')
    this.checkStatus()
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  },

  methods: {
    // 检查登录状态和家庭状态
    async checkStatus() {
      if (!userManager.isLoggedIn()) {
        // 未登录，跳转到登录页
        uni.reLaunch({
          url: '/pages/login/index'
        })
        return
      }

      // 已登录，检查家庭状态
      try {
        const uuid = userManager.getUuid()
        if (!uuid) {
          uni.reLaunch({ url: '/pages/login/index' })
          return
        }

        const familyInfo = await familyApi.getFamilyInfo(uuid)

        if (familyInfo) {
          // 已加入家庭，保存家庭信息到本地
          userManager.saveUserInfo({
            ...userManager.getUserInfo(),
            familyId: familyInfo.id,
            familyName: familyInfo.name,
            isAdmin: familyInfo.isAdmin
          })
          console.log('用户已加入家庭:', familyInfo.name)
        } else {
          // 未加入家庭，跳转到家庭页
          console.log('用户未加入家庭，跳转到家庭页')
          uni.reLaunch({
            url: '/pages/family/index'
          })
        }
      } catch (e) {
        console.error('检查家庭状态失败', e)
        // 出错也跳转到家庭页
        uni.reLaunch({
          url: '/pages/family/index'
        })
      }
    }
  }
}
</script>

<style>
@import './app.css';
</style>