<script>
import userManager from '@/utils/user.js'
import { familyApi } from '@/utils/api.js'

export default {
  onLaunch: function() {
    this.checkStatus()
  },
  onShow: function() {},
  onHide: function() {},

  methods: {
    async checkStatus() {
      if (!userManager.isLoggedIn()) {
        uni.reLaunch({ url: '/pages/login/index' })
        return
      }

      try {
        const familyInfo = await familyApi.getFamilyInfo()
        if (familyInfo) {
          userManager.saveUserInfo({
            ...userManager.getUserInfo(),
            familyId: familyInfo.id,
            familyName: familyInfo.name,
            isAdmin: familyInfo.isAdmin
          })
        } else {
          uni.reLaunch({ url: '/pages/family/index' })
        }
      } catch (e) {
        console.error('检查家庭状态失败', e)
        uni.reLaunch({ url: '/pages/family/index' })
      }
    }
  }
}
</script>

<style>
@import './app.css';
</style>
