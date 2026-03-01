<template>
  <div>
    <h1 class="page-title">管理后台</h1>
    <div class="admin-nav">
      <router-link to="/admin">仪表盘</router-link>
      <router-link to="/admin/users">用户</router-link>
      <router-link to="/admin/audit">审核</router-link>
      <router-link to="/admin/announcements">公告</router-link>
      <router-link to="/" style="margin-left:auto">← 前台</router-link>
    </div>
    <div style="display:grid; grid-template-columns:repeat(auto-fill,minmax(150px,1fr)); gap:12px; margin-bottom:20px">
      <div v-for="(s,i) in statCards" :key="i" class="card" style="text-align:center">
        <div style="font-size:24px; font-weight:700; color:#1a73e8">{{ s.value }}</div>
        <div style="font-size:12px; color:#999">{{ s.label }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../api/request'
const stats = ref({})
const statCards = computed(() => [
  {label:'用户',value:stats.value.userCount||0}, {label:'帖子',value:stats.value.postCount||0},
  {label:'评论',value:stats.value.commentCount||0}, {label:'钓点',value:stats.value.spotCount||0},
  {label:'百科',value:stats.value.wikiCount||0}, {label:'待审举报',value:stats.value.pendingReports||0},
])
onMounted(async () => { const r = await request.get('/api/admin/statistics'); if(r.code===200) stats.value=r.data })
</script>

<style scoped>
.admin-nav { display: flex; gap: 12px; margin-bottom: 16px; padding: 8px 0; border-bottom: 1px solid #eee; font-size: 14px; }
.admin-nav a { color: #555; text-decoration: none; }
.admin-nav a:hover, .admin-nav a.router-link-exact-active { color: #1a73e8; }
</style>
