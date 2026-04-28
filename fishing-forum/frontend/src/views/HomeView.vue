<template>
  <div class="page-shell">
    <section class="hero-panel home-hero">
      <div>
        <p class="eyebrow">钓友圈 · Fishing Community</p>
        <h1 class="hero-title">找钓点、聊装备、晒渔获。</h1>
        <p class="hero-subtitle">把每一次出钓经验沉淀成可搜索、可讨论、可复用的社区内容。</p>
      </div>
      <router-link class="hero-action" :to="{ path: '/post/create', query: { postType: 'CATCH' } }"><el-button size="large" round>发布渔获</el-button></router-link>
    </section>
    <!-- 公告 -->
    <div v-if="announcements.length" class="card" style="background:#fffbe6; border-color:#ffe58f">
      <div v-for="a in announcements" :key="a.id" style="font-size:13px; padding:2px 0">📢 <b>{{ a.title }}</b> — {{ a.content }}</div>
    </div>

    <!-- 统计 -->
    <div class="stat-row">
      <div class="stat-item"><div class="stat-num">{{ stats.postCount || 0 }}</div><div class="stat-label">帖子</div></div>
      <div class="stat-item"><div class="stat-num">{{ stats.userCount || 0 }}</div><div class="stat-label">用户</div></div>
      <div class="stat-item"><div class="stat-num">{{ stats.spotCount || 0 }}</div><div class="stat-label">钓点</div></div>
      <div class="stat-item"><div class="stat-num">{{ stats.wikiCount || 0 }}</div><div class="stat-label">词条</div></div>
    </div>

    <div class="home-grid responsive-grid">
      <!-- 帖子列表 -->
      <div class="main-col">
        <h2 class="page-title">最新帖子</h2>
        <div v-for="post in posts" :key="post.id" class="card post-item list-card fish-feed-card" @click="$router.push(`/post/${post.id}`)">
          <div class="card-header post-card-header">
            <img :src="post.authorAvatar || '/default-avatar.png'" class="avatar-sm" />
            <div class="post-author-block">
              <span class="text-link" @click.stop="$router.push(`/profile/${post.userId}`)">{{ post.authorName }}</span>
              <span class="text-muted post-date">{{ formatTime(post.createdAt) }}</span>
            </div>
            <div class="post-tags">
              <el-tag v-if="post.postType==='CATCH'" size="small" type="success">🐟 渔获</el-tag>
              <el-tag v-if="post.postType==='REVIEW'" size="small" type="warning">⭐ 测评</el-tag>
              <el-tag v-if="post.isTop" size="small" type="danger">置顶</el-tag>
              <el-tag v-if="post.isFeatured" size="small" type="warning">精华</el-tag>
            </div>
          </div>
          <div class="post-title">{{ post.title }}</div>
          <div class="post-excerpt">{{ stripHtml(post.content) }}</div>
          <div class="fish-tags">
            <span class="fish-pill section-pill">{{ sectionIcon(post.sectionName) }} {{ post.sectionName || '钓友交流' }}</span>
            <span v-if="post.tagName" class="fish-pill">#{{ post.tagName }}</span>
            <span v-if="post.postType==='CATCH'" class="fish-pill catch-pill">{{ catchHint(post) }}</span>
            <span v-if="isHot(post)" class="post-hot-badge">水面热帖</span>
          </div>
          <div class="post-meta mobile-post-stats">
            <span>👁 {{ post.viewCount }}</span>
            <span>💬 {{ post.commentCount }}</span>
            <span>👍 {{ post.likeCount }}</span>
          </div>
        </div>
        <el-empty v-if="!posts.length" description="暂无帖子，快来发第一帖吧！" />
      </div>

      <!-- 侧边栏 -->
      <div class="side-col">
        <!-- 板块 -->
        <div class="card">
          <h3 style="font-size:14px; margin-bottom:10px">📋 板块导航</h3>
          <div v-for="s in sections" :key="s.id" class="side-item">
            <router-link :to="`/forum?sectionId=${s.id}`">{{ s.icon }} {{ s.name }}</router-link>
            <span class="text-muted">{{ s.postCount || 0 }}</span>
          </div>
        </div>

        <!-- 热门帖子 -->
        <div class="card" v-if="hotPosts.length">
          <h3 style="font-size:14px; margin-bottom:10px">🔥 热门帖子</h3>
          <div v-for="(hp, i) in hotPosts" :key="hp.id" class="side-item" style="cursor:pointer" @click="$router.push(`/post/${hp.id}`)">
            <span class="hot-rank" :class="'rank-'+(i+1)">{{ i+1 }}</span>
            <span style="font-size:13px; flex:1; overflow:hidden; text-overflow:ellipsis; white-space:nowrap">{{ hp.title }}</span>
            <span class="text-muted" style="flex-shrink:0">{{ hp.viewCount }}</span>
          </div>
        </div>

        <!-- 热门标签 -->
        <div class="card" v-if="hotTags.length">
          <h3 style="font-size:14px; margin-bottom:10px">🏷️ 热门标签</h3>
          <div class="tag-cloud">
            <router-link v-for="tag in hotTags" :key="tag.id" :to="`/forum?tagId=${tag.id}`" class="tag-item" :style="{background:tag.color+'20', color:tag.color, borderColor:tag.color+'40'}">
              #{{ tag.name }}
            </router-link>
          </div>
        </div>

        <!-- 快捷入口 -->
        <div class="card">
          <h3 style="font-size:14px; margin-bottom:10px">🔗 快捷入口</h3>
          <router-link to="/post/create" class="quick-link">📝 发表帖子</router-link>
          <router-link to="/forum?postType=CATCH" class="quick-link">🐟 渔获日记</router-link>
          <router-link to="/forum?postType=REVIEW" class="quick-link">⭐ 装备测评</router-link>
          <router-link to="/spots" class="quick-link">📍 钓点推荐</router-link>
          <router-link to="/wiki" class="quick-link">📖 钓鱼百科</router-link>
          <router-link to="/weather" class="quick-link">🌤 天气查询</router-link>
        </div>

        <!-- 活跃用户 -->
        <div class="card" v-if="activeUsers.length">
          <h3 style="font-size:14px; margin-bottom:10px">🏆 活跃钓友</h3>
          <div v-for="u in activeUsers" :key="u.id" class="side-item" style="cursor:pointer" @click="$router.push(`/profile/${u.id}`)">
            <img :src="u.avatar || '/default-avatar.png'" class="avatar-sm" />
            <span style="font-size:13px">{{ u.username }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request'

const posts = ref([]), sections = ref([]), announcements = ref([]), stats = ref({})
const activeUsers = ref([]), hotPosts = ref([]), hotTags = ref([])

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t), now = new Date(), diff = (now - d) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  return d.toLocaleDateString('zh-CN')
}
const stripHtml = (h) => h ? h.replace(/<[^>]+>/g, '').substring(0, 120) : ''
const sectionIcon = (name = '') => {
  if (name.includes('渔获')) return '🐟'
  if (name.includes('装备')) return '🎣'
  if (name.includes('技巧')) return '🪝'
  if (name.includes('钓点')) return '📍'
  return '🌊'
}
const catchHint = (post) => {
  const text = `${post.title || ''} ${stripHtml(post.content || '')}`
  for (const word of ['鲫鱼', '翘嘴', '草鱼', '黑坑', '野钓']) {
    if (text.includes(word)) return word
  }
  return '鱼获分享'
}
const isHot = (post) => (post.viewCount || 0) >= 200 || (post.likeCount || 0) >= 20

onMounted(async () => {
  const [p, s, a, hp, ht] = await Promise.all([
    request.get('/api/posts', { params: { page: 1, size: 10 } }),
    request.get('/api/sections'),
    request.get('/api/announcements'),
    request.get('/api/posts/hot', { params: { limit: 5 } }),
    request.get('/api/tags/hot', { params: { limit: 15 } })
  ])
  if (p.code === 200) posts.value = p.data.records || []
  if (s.code === 200) sections.value = s.data || []
  if (a.code === 200) announcements.value = (a.data || []).filter(x => x.isActive)
  if (hp.code === 200) hotPosts.value = hp.data || []
  if (ht.code === 200) hotTags.value = ht.data || []
  // 获取统计数据（使用公开接口）
  try { const r = await request.get('/api/statistics/public'); if (r.code === 200) stats.value = r.data } catch (e) {
    stats.value = { postCount: posts.value.length, userCount: '-', spotCount: '-', wikiCount: '-' }
  }
})
</script>

<style scoped>
.home-hero { display: flex; align-items: center; justify-content: space-between; gap: 18px; max-width: 100%; }
.home-hero > div { min-width: 0; max-width: 100%; }
.hero-action { position: relative; z-index: 1; flex-shrink: 0; }
.eyebrow { position: relative; z-index: 1; font-size: 12px; color: rgba(255,255,255,.68); text-transform: uppercase; letter-spacing: .12em; margin-bottom: 8px; }
.home-grid { align-items: start; }
.main-col { min-width: 0; }
.post-item { cursor: pointer; transition: box-shadow 0.15s; }
.post-item:hover { box-shadow: 0 10px 24px rgba(30, 64, 96, .08); }
.fish-feed-card { border-color: #e3eaf2; box-shadow: 0 8px 20px rgba(15, 23, 42, .045); margin-bottom: 14px; }
.post-card-header { align-items: center; }
.post-author-block { display: flex; flex-direction: column; min-width: 0; }
.post-date { color: #9aa7b4; line-height: 1.2; }
.post-title { font-size: 18px; font-weight: 700; margin-bottom: 6px; color: var(--ink); line-height: 1.35; }
.post-excerpt { font-size: 13px; color: #777; margin-bottom: 8px; line-height: 1.5; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.post-meta { font-size: 12px; color: #9aa7b4; display: flex; gap: 12px; }
.post-tags { margin-left: auto; display: flex; gap: 4px; flex-wrap: wrap; justify-content: flex-end; }
.fish-tags { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; margin: 8px 0 10px; }
.fish-pill { display: inline-flex; align-items: center; min-height: 24px; padding: 3px 9px; border: 1px solid #d8e7f4; border-radius: 999px; background: #f4f8fc; color: #4f6f92; font-size: 12px; line-height: 1; }
.section-pill { background: #edf5ff; color: var(--green-dark); }
.catch-pill { background: #f0f9f6; border-color: #cbe9dc; color: #287459; }
.post-hot-badge { display: inline-flex; min-height: 24px; align-items: center; padding: 3px 8px; border-radius: 999px; background: #fff7ed; color: #b66a18; font-size: 12px; }
.mobile-post-stats { padding-top: 10px; border-top: 1px solid #edf2f7; justify-content: flex-start; }
.side-item { display: flex; align-items: center; gap: 8px; padding: 5px 0; font-size: 13px; }
.side-item a { color: #555; flex: 1; }
.side-item a:hover { color: #1a73e8; }
.quick-link { display: block; padding: 5px 0; font-size: 13px; color: #555; }
.quick-link:hover { color: #1a73e8; }
.hot-rank { width: 18px; height: 18px; border-radius: 3px; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; background: #f0f0f0; color: #999; flex-shrink: 0; }
.rank-1 { background: #ff4d4f; color: #fff; }
.rank-2 { background: #ff7a45; color: #fff; }
.rank-3 { background: #ffa940; color: #fff; }
.tag-cloud { display: flex; flex-wrap: wrap; gap: 6px; }
.tag-item { padding: 2px 8px; border-radius: 12px; font-size: 12px; border: 1px solid; text-decoration: none; transition: opacity 0.15s; }
.tag-item:hover { opacity: 0.7; }
@media (max-width: 768px) {
  .home-hero { align-items: flex-start; flex-direction: column; }
  .hero-action { width: auto; }
  .post-tags { margin-left: auto; justify-content: flex-end; }
  .post-meta { flex-wrap: wrap; gap: 8px; }
  .post-card-header .avatar-sm { flex: 0 0 auto; }
  .fish-feed-card { padding: 15px; }
}
</style>
