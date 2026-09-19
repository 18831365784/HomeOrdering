<template>
  <text class="status-badge" :class="'status-' + tone">{{ label }}</text>
</template>

<script>
const STATUS_MAP = {
  '-2': { label: '已拒绝', tone: 'danger' },
  '-1': { label: '已取消', tone: 'muted' },
  '0': { label: '待接单', tone: 'warning' },
  '1': { label: '制作中', tone: 'primary' },
  '2': { label: '已完成', tone: 'success' }
}

export default {
  name: 'StatusBadge',
  props: {
    status: { type: [Number, String], required: true }
  },
  computed: {
    meta() {
      return STATUS_MAP[String(this.status)] || { label: '未知', tone: 'muted' }
    },
    label() {
      return this.meta.label
    },
    tone() {
      return this.meta.tone
    }
  }
}
</script>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
}
.status-primary {
  background: rgba(184, 92, 56, 0.12);
  color: #B85C38;
}
.status-success {
  background: rgba(92, 107, 90, 0.12);
  color: #5C6B5A;
}
.status-warning {
  background: rgba(196, 122, 44, 0.14);
  color: #C47A2C;
}
.status-danger {
  background: rgba(179, 58, 43, 0.12);
  color: #B33A2B;
}
.status-muted {
  background: rgba(154, 144, 134, 0.14);
  color: #9A9086;
}
</style>
