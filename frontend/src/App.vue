<script>
import userManager from '@/utils/user.js'
import { familyApi } from '@/utils/api.js'

export default {
  onLaunch: function() {
    // 原生窗口底色（与 CSS 无关），避免默认白色顶进导航栏
    this.applyWindowBg()
    this.checkStatus()
  },
  onShow: function() {
    this.applyWindowBg()
  },
  onHide: function() {},

  methods: {
    applyWindowBg() {
      try {
        uni.setBackgroundColor({
          backgroundColor: '#F7F3EE',
          backgroundColorTop: '#F7F3EE',
          backgroundColorBottom: '#F7F3EE'
        })
      } catch (e) {}
    },

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
