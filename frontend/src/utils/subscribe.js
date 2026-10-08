/**
 * 制作提醒的一次性订阅。必须在点击回调里同步调用，不能先 await。
 * 每同意一次，服务端才能再发一条服务通知。
 */
export function requestMakerSubscribe(templateId) {
  if (!templateId) {
    uni.showToast({ title: '提醒尚未开通', icon: 'none' })
    return
  }
  uni.requestSubscribeMessage({
    tmplIds: [templateId],
    success(res) {
      const state = res[templateId]
      if (state === 'accept') {
        uni.showToast({ title: '已增加1次提醒', icon: 'none' })
      } else if (state === 'ban') {
        uni.showToast({ title: '请打开订阅消息', icon: 'none' })
      } else {
        uni.showToast({ title: '这次未开启提醒', icon: 'none' })
      }
    },
    fail(err) {
      const msg = (err && err.errMsg) || ''
      let title = '暂时无法开启'
      if (/gesture|tap/i.test(msg)) title = '请点这一行开启'
      else if (/template/i.test(msg)) title = '订阅模板无效'
      else if (/switch|20004/i.test(msg)) title = '请打开订阅消息'
      uni.showToast({ title, icon: 'none' })
      console.error('订阅失败', msg)
    }
  })
}
