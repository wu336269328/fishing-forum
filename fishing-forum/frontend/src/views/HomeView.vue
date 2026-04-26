<template>
  <div class="page-shell">
    <section class="hero-panel home-hero">
      <div>
        <p class="eyebrow">钓友圈 · Fishing Community</p>
        <h1 class="hero-title">找钓点、聊装备、晒渔获。</h1>
        <p class="hero-subtitle">把每一次出钓经验沉淀成可搜索、可讨论、可复用的社区内容。</p>
      </div>
      <router-link to="/post/create"><el-button size="large" round>发布渔获</el-button></router-link>
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
        <div v-for="post in posts" :key="post.id" class="card post-item list-card" @click="$router.push(`/post/${post.id}`)">
          <div class="card-header">
            <img :src="post.authorAvatar || '/default-avatar.png'" class="avatar-sm" />
            <div>
              <span class="text-link" @click.stop="$router.push(`/profile/${post.userId}`)">{{ post.authorName }}</span>
              <span class="text-muted" style="margin-left:6px">{{ formatTime(post.createdAt) }}</span>
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
          <div class="post-meta">
            <span>{{ post.sectionName }}</span>
            <span v-if="post.tagName" style="color:#1a73e8">#{{ post.tagName }}</span>
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
.home-hero { display: flex; align-items: center; justify-content: space-between; gap: 18px; }
.eyebrow { position: relative; z-index: 1; font-size: 12px; color: rgba(255,255,255,.68); text-transform: uppercase; letter-spacing: .12em; margin-bottom: 8px; }
.home-grid { align-items: start; }
.post-item { cursor: pointer; transition: box-shadow 0.15s; }
.post-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.post-title { font-size: 17px; font-weight: 800; margin-bottom: 4px; color: var(--ink); }
.post-excerpt { font-size: 13px; color: #777; margin-bottom: 8px; line-height: 1.5; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.post-meta { font-size: 12px; color: #999; display: flex; gap: 12px; }
.post-tags { margin-left: auto; display: flex; gap: 4px; flex-wrap: wrap; justify-content: flex-end; }
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
  .post-tags { width: 100%; margin-left: 42px; justify-content: flex-start; }
  .post-meta { flex-wrap: wrap; gap: 8px; }
}
</style>
