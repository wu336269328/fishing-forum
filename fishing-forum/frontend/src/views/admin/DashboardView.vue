<template>
  <div class="page-shell admin-page">
    <section class="hero-panel admin-hero">
      <p class="eyebrow">Admin Console</p>
      <h1 class="hero-title">管理后台</h1>
      <p class="hero-subtitle">快速查看社区规模、待处理举报和运营入口。</p>
    </section>
    <div class="admin-nav">
      <router-link to="/admin">仪表盘</router-link>
      <router-link to="/admin/users">用户</router-link>
      <router-link to="/admin/audit">审核</router-link>
      <router-link to="/admin/announcements">公告</router-link>
      <router-link to="/" style="margin-left:auto">← 前台</router-link>
    </div>
    <div class="admin-grid">
      <div v-for="(s,i) in statCards" :key="i" class="card admin-stat">
        <div class="admin-value">{{ s.value }}</div>
        <div class="text-muted">{{ s.label }}</div>
      </div>
    </div>
    <div class="card admin-note">
      <h3>待优化运营动作</h3>
      <p>优先处理举报、异常用户、封禁禁言和敏感词维护。所有关键后台操作会写入操作日志。</p>
    </div>
    <div class="card">
      <h3 style="font-size:16px;margin-bottom:10px">最近操作日志</h3>
      <div v-for="log in logs" :key="log.id" class="log-row">
        <span>{{ log.action }}</span>
        <b>{{ log.targetType }} #{{ log.targetId || '-' }}</b>
        <em>{{ log.detail }}</em>
      </div>
      <el-empty v-if="!logs.length" description="暂无日志" :image-size="40" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../api/request'
const stats = ref({})
const logs = ref([])
const statCards = computed(() => [
  {label:'用户',value:stats.value.userCount||0}, {label:'帖子',value:stats.value.postCount||0},
  {label:'评论',value:stats.value.commentCount||0}, {label:'钓点',value:stats.value.spotCount||0},
  {label:'百科',value:stats.value.wikiCount||0}, {label:'待审举报',value:stats.value.pendingReports||0},
  {label:'封禁用户',value:stats.value.bannedUsers||0}, {label:'禁言用户',value:stats.value.mutedUsers||0},
  {label:'敏感词',value:stats.value.sensitiveWords||0},
])
onMounted(async () => {
  const r = await request.get('/api/admin/statistics'); if(r.code===200) stats.value=r.data
  const l = await request.get('/api/admin/logs', { params: { page: 1, size: 8 } }); if(l.code===200) logs.value=l.data.records||[]
})
</script>

<style scoped>
.eyebrow { position: relative; z-index: 1; font-size: 12px; color: rgba(255,255,255,.68); text-transform: uppercase; letter-spacing: .12em; margin-bottom: 8px; }
.admin-nav { display: flex; gap: 12px; margin-bottom: 16px; padding: 10px; border: 1px solid var(--line); border-radius: 16px; background: rgba(255,255,255,.72); font-size: 14px; overflow-x: auto; }
.admin-nav a { color: #555; text-decoration: none; }
.admin-nav a:hover, .admin-nav a.router-link-exact-active { color: #1a73e8; }
.admin-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(150px,1fr)); gap:12px; margin-bottom:20px; }
.admin-stat { text-align:center; }
.admin-value { font-size: 28px; font-weight: 900; color: var(--blue); }
.admin-note h3 { font-size: 16px; margin-bottom: 6px; }
.admin-note p { color: var(--muted); font-size: 13px; }
.log-row { display:grid; grid-template-columns:140px 120px 1fr; gap:10px; padding:8px 0; border-bottom:1px solid var(--line); font-size:13px; }
.log-row em { color: var(--muted); font-style: normal; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
@media (max-width:768px){ .log-row{grid-template-columns:1fr} }
</style>
