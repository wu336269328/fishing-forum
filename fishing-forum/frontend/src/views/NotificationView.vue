<template>
  <div class="page-shell notification-page">
    <div class="notification-header">
      <h1 class="page-title notification-title">通知</h1>
      <el-button text size="small" @click="markAllRead" v-if="notifications.length">全部已读</el-button>
    </div>
    <div
      v-for="n in notifications"
      :key="n.id"
      class="card notification-card"
      :class="{ unread: !n.isRead }"
      role="button"
      tabindex="0"
      @click="markRead(n)"
      @keydown.enter.space.prevent="markRead(n)"
    >
      <div class="notification-card-title">{{ n.title }}</div>
      <div class="notification-card-content">{{ n.content }}</div>
      <div class="notification-card-time">{{ new Date(n.createdAt).toLocaleString('zh-CN') }}</div>
    </div>
    <div v-if="!notifications.length" class="card empty-state">
      <div class="empty-state-icon">🔔</div>
      <div class="empty-state-title">还没有新通知</div>
      <div class="empty-state-text">点赞、评论、关注的更新会出现在这里。</div>
    </div>
    <div class="pagination-wrap" v-if="total>20"><el-pagination background layout="prev,pager,next" :total="total" :page-size="20" v-model:current-page="page" @current-change="load" /></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const notifications = ref([]), page = ref(1), total = ref(0)
const load = async () => { const r = await request.get('/api/notifications',{params:{page:page.value,size:20}}); if(r.code===200){ notifications.value=r.data.records||[]; total.value=r.data.total||0 } }
const markRead = async (n) => {
  if(!n.isRead){ await request.put(`/api/notifications/${n.id}/read`); n.isRead=true }
  if (['LIKE','COMMENT','COMMENT_REPLY','MENTION'].includes(n.type) && n.relatedId) router.push(`/post/${n.relatedId}`)
  else if (n.type === 'FOLLOW' && n.relatedId) router.push(`/profile/${n.relatedId}`)
}
const markAllRead = async () => { await request.put('/api/notifications/read-all'); notifications.value.forEach(n=>n.isRead=true); ElMessage.success('全部已读') }
onMounted(load)
</script>

<style scoped>
.notification-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.notification-title { margin: 0; }
.notification-card { cursor: pointer; transition: border-color .15s, box-shadow .15s; }
.notification-card:hover { border-color: var(--color-primary); box-shadow: var(--shadow-hover); }
.notification-card.unread { border-left: 3px solid var(--color-primary); }
.notification-card-title { font-size: 14px; font-weight: 700; color: var(--ink); }
.notification-card-content { font-size: 13px; color: var(--muted); margin-top: 4px; line-height: 1.5; }
.notification-card-time { font-size: 12px; color: var(--muted-soft); margin-top: 6px; }
</style>
