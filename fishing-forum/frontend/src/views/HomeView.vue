<template>
  <div class="page-shell">
    <section class="hero-panel home-hero">
      <div>
        <p class="eyebrow">钓友圈 · Fishing Community</p>
        <h1 class="hero-title">找钓点、聊装备、晒渔获。</h1>
        <p class="hero-subtitle">把每一次出钓经验沉淀成可搜索、可讨论、可复用的社区内容。</p>
      </div>
      <div class="hero-actions">
        <router-link class="hero-action" :to="{ path: '/post/create', query: { postType: 'CATCH' } }"><el-button size="large" round>发布渔获</el-button></router-link>
        <router-link class="hero-action" to="/forum"><el-button size="large" plain round>浏览论坛</el-button></router-link>
      </div>
    </section>
    <!-- 移动端快捷入口（PC 通过侧栏访问，手机隐藏侧栏，所以补一行） -->
    <nav class="mobile-quick-row" aria-label="快捷入口">
      <router-link to="/spots" class="mobile-quick-tile">
        <span class="mobile-quick-icon">📍</span>
        <span class="mobile-quick-label">钓点</span>
      </router-link>
      <router-link to="/wiki" class="mobile-quick-tile">
        <span class="mobile-quick-icon">📖</span>
        <span class="mobile-quick-label">百科</span>
      </router-link>
      <router-link to="/weather" class="mobile-quick-tile">
        <span class="mobile-quick-icon">🌤</span>
        <span class="mobile-quick-label">天气</span>
      </router-link>
      <router-link to="/forum?postType=CATCH" class="mobile-quick-tile">
        <span class="mobile-quick-icon">🎣</span>
        <span class="mobile-quick-label">渔获</span>
      </router-link>
    </nav>

    <!-- 公告 -->
    <div v-if="announcements.length" class="card notice-card home-notice">
      <div v-for="a in announcements" :key="a.id" class="notice-row"><b>{{ a.title }}</b><span>{{ a.content }}</span></div>
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
        <article
          v-for="post in posts"
          :key="post.id"
          class="card post-item list-card fish-feed-card"
          role="button"
          tabindex="0"
          @click="$router.push(`/post/${post.id}`)"
          @keydown.enter.space.prevent="$router.push(`/post/${post.id}`)"
        >
          <div class="card-header post-card-header">
            <img :src="post.authorAvatar || '/default-avatar.png'" class="avatar-sm" loading="lazy" />
            <div class="post-author-block">
              <span class="text-link" @click.stop="$router.push(`/profile/${post.userId}`)">{{ post.authorName }}</span>
              <span class="text-muted post-date">{{ formatTime(post.createdAt) }}</span>
            </div>
            <div class="post-tags">
              <el-tag v-if="post.postType==='CATCH'" size="small" type="success">渔获</el-tag>
              <el-tag v-if="post.postType==='REVIEW'" size="small" type="warning">测评</el-tag>
              <el-tag v-if="post.isTop" size="small" type="danger">置顶</el-tag>
              <el-tag v-if="post.isFeatured" size="small" type="warning">精华</el-tag>
            </div>
          </div>
          <div class="post-title">{{ post.title }}</div>
          <div class="post-excerpt">{{ stripHtml(post.content) }}</div>
          <div class="fish-tags">
            <span class="ui-chip ui-chip--section">{{ sectionIcon(post.sectionName) }} {{ post.sectionName || '钓友交流' }}</span>
            <span v-if="post.tagName" class="ui-chip">#{{ post.tagName }}</span>
            <span v-if="post.postType==='CATCH'" class="ui-chip ui-chip--success">{{ catchHint(post) }}</span>
            <span v-if="isHot(post)" class="ui-chip ui-chip--warning">水面热帖</span>
          </div>
          <div class="post-meta mobile-post-stats">
            <span>浏览 {{ post.viewCount }}</span>
            <span>评论 {{ post.commentCount }}</span>
            <span>点赞 {{ post.likeCount }}</span>
          </div>
        </article>
        <div v-if="loadError" class="card desktop-error-card">
          <div>
            <b>内容加载失败</b>
            <p class="text-muted">后端暂时不可用，刷新或稍后重试。</p>
          </div>
          <el-button size="small" type="primary" @click="retryHomeData">重新加载</el-button>
        </div>
        <div v-else-if="!posts.length" class="card desktop-empty-card">
          <el-empty description="暂无帖子，快来发第一帖吧！" />
        </div>
      </div>

      <!-- 侧边栏 -->
      <div class="side-col">
        <!-- 板块 -->
        <div class="card side-card">
          <h3 class="side-title">板块导航</h3>
          <div v-for="s in sections" :key="s.id" class="side-item">
            <router-link :to="`/forum?sectionId=${s.id}`">{{ s.icon }} {{ s.name }}</router-link>
            <span class="text-muted">{{ s.postCount || 0 }}</span>
          </div>
        </div>

        <!-- 热门帖子 -->
        <div class="card side-card" v-if="hotPosts.length">
          <h3 class="side-title">热门帖子</h3>
          <div
            v-for="(hp, i) in hotPosts"
            :key="hp.id"
            class="side-item hot-side-item"
            role="button"
            tabindex="0"
            @click="$router.push(`/post/${hp.id}`)"
            @keydown.enter.space.prevent="$router.push(`/post/${hp.id}`)"
          >
            <span class="hot-rank" :class="'rank-'+(i+1)">{{ i+1 }}</span>
            <span class="side-post-title">{{ hp.title }}</span>
            <span class="text-muted side-count">{{ hp.viewCount }}</span>
          </div>
        </div>

        <!-- 热门标签 -->
        <div class="card side-card" v-if="hotTags.length">
          <h3 class="side-title">热门标签</h3>
          <div class="tag-cloud">
            <router-link v-for="tag in hotTags" :key="tag.id" :to="`/forum?tagId=${tag.id}`" class="tag-item" :style="{background:tag.color+'20', color:tag.color, borderColor:tag.color+'40'}">
              #{{ tag.name }}
            </router-link>
          </div>
        </div>

        <!-- 快捷入口 -->
        <div class="card side-card">
          <h3 class="side-title">快捷入口</h3>
          <router-link to="/post/create" class="quick-link">发表帖子</router-link>
          <router-link to="/forum?postType=CATCH" class="quick-link">渔获日记</router-link>
          <router-link to="/forum?postType=REVIEW" class="quick-link">装备测评</router-link>
          <router-link to="/spots" class="quick-link">钓点推荐</router-link>
          <router-link to="/wiki" class="quick-link">钓鱼百科</router-link>
          <router-link to="/weather" class="quick-link">天气查询</router-link>
        </div>

        <!-- 活跃用户 -->
        <div class="card side-card" v-if="activeUsers.length">
          <h3 class="side-title">活跃钓友</h3>
          <div
            v-for="u in activeUsers"
            :key="u.id"
            class="side-item active-user-item"
            role="button"
            tabindex="0"
            @click="$router.push(`/profile/${u.id}`)"
            @keydown.enter.space.prevent="$router.push(`/profile/${u.id}`)"
          >
            <img :src="u.avatar || '/default-avatar.png'" class="avatar-sm" loading="lazy" />
            <span class="side-user-name">{{ u.username }}</span>
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
const loadError = ref(false)

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

const loadHomeData = async () => {
  loadError.value = false
  const [p, s, a, hp, ht] = await Promise.allSettled([
    request.get('/api/posts', { params: { page: 1, size: 10 } }),
    request.get('/api/sections'),
    request.get('/api/announcements'),
    request.get('/api/posts/hot', { params: { limit: 5 } }),
    request.get('/api/tags/hot', { params: { limit: 15 } })
  ])
  const valueOf = (result) => result.status === 'fulfilled' ? result.value : null
  const postResult = valueOf(p), sectionResult = valueOf(s), announcementResult = valueOf(a), hotResult = valueOf(hp), tagResult = valueOf(ht)
  loadError.value = [p, s, a, hp, ht].some(result => result.status === 'rejected')
  if (postResult?.code === 200) posts.value = postResult.data.records || []
  if (sectionResult?.code === 200) sections.value = sectionResult.data || []
  if (announcementResult?.code === 200) announcements.value = (announcementResult.data || []).filter(x => x.isActive)
  if (hotResult?.code === 200) hotPosts.value = hotResult.data || []
  if (tagResult?.code === 200) hotTags.value = tagResult.data || []
  try { const r = await request.get('/api/statistics/public'); if (r.code === 200) stats.value = r.data } catch (e) {
    stats.value = { postCount: posts.value.length, userCount: '-', spotCount: '-', wikiCount: '-' }
  }
}
const retryHomeData = loadHomeData

onMounted(loadHomeData)
</script>

<style scoped>
.mobile-quick-row { display: none; }
.home-hero { display: flex; align-items: center; justify-content: space-between; gap: 18px; max-width: 100%; }
.home-hero > div { min-width: 0; max-width: 100%; }
.hero-actions { position: relative; z-index: 1; display: flex; gap: 10px; flex-wrap: wrap; flex-shrink: 0; }
.hero-action { flex-shrink: 0; }
.home-notice { display: grid; gap: 6px; }
.notice-row { display: flex; gap: 8px; align-items: baseline; font-size: 13px; }
.notice-row b::before { content: "公告"; display: inline-flex; margin-right: 8px; padding: 2px 7px; border-radius: 999px; background: rgba(201, 131, 23, .14); color: var(--color-warning); font-size: 11px; }
.home-grid { align-items: start; }
.main-col { min-width: 0; }
.post-item { cursor: pointer; transition: box-shadow 0.15s; }
.post-item:hover { box-shadow: var(--shadow-hover); }
.fish-feed-card { border-color: var(--border-subtle); box-shadow: var(--shadow-card); margin-bottom: 14px; }
.post-card-header { align-items: center; }
.post-author-block { display: flex; flex-direction: column; min-width: 0; }
.post-date { color: var(--muted-soft); line-height: 1.2; }
.post-title { font-size: 18px; font-weight: 800; margin-bottom: 6px; color: var(--ink); line-height: 1.35; letter-spacing: -0.01em; }
.post-excerpt { font-size: 13px; color: var(--muted); margin-bottom: 8px; line-height: 1.5; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.post-meta { font-size: 12px; color: var(--muted-soft); display: flex; gap: 12px; }
.post-tags { margin-left: auto; display: flex; gap: 4px; flex-wrap: wrap; justify-content: flex-end; }
.fish-tags { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; margin: 8px 0 10px; }
.mobile-post-stats { padding-top: 10px; border-top: 1px solid var(--border-subtle); justify-content: flex-start; }
.side-title { font-size: 14px; font-weight: 700; margin-bottom: 10px; color: var(--ink); }
.side-card { border-color: var(--border-subtle); }
.side-item { display: flex; align-items: center; gap: 8px; padding: 6px 0; font-size: 13px; border-radius: 8px; }
.side-item a { color: var(--ink); flex: 1; }
.side-item a:hover { color: var(--color-primary); }
.hot-side-item, .active-user-item { cursor: pointer; }
.hot-side-item:hover, .active-user-item:hover { background: var(--color-primary-soft); padding-left: 6px; padding-right: 6px; }
.side-post-title { font-size: 13px; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--ink); }
.side-count { flex-shrink: 0; color: var(--muted); }
.side-user-name { font-size: 13px; color: var(--ink); }
.quick-link { display: block; padding: 7px 0; font-size: 13px; color: var(--ink); border-radius: 6px; }
.quick-link:hover { color: var(--color-primary); padding-left: 4px; }
.hot-rank { width: 20px; height: 20px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 800; background: #eef2f7; color: var(--muted); flex-shrink: 0; }
.rank-1 { background: var(--rank-1); color: #fff; }
.rank-2 { background: var(--rank-2); color: #fff; }
.rank-3 { background: var(--rank-3); color: #fff; }
.tag-cloud { display: flex; flex-wrap: wrap; gap: 6px; }
.tag-item { padding: 4px 10px; border-radius: 999px; font-size: 12px; border: 1px solid; text-decoration: none; transition: opacity 0.15s, transform .15s; min-height: 24px; display: inline-flex; align-items: center; }
.tag-item:hover { opacity: 0.78; transform: translateY(-1px); }
.mobile-quick-tile { display: flex; flex-direction: column; align-items: center; gap: 4px; padding: 10px 6px; border-radius: 14px; background: var(--surface-card); border: 1px solid var(--border-subtle); color: var(--ink); font-size: 12px; font-weight: 600; min-height: 64px; transition: transform .15s, box-shadow .15s, border-color .15s; }
.mobile-quick-tile:hover { transform: translateY(-1px); border-color: var(--color-primary); box-shadow: var(--shadow-card); }
.mobile-quick-icon { font-size: 22px; line-height: 1; }
.mobile-quick-label { line-height: 1.1; }

@media (max-width: 768px) {
  .home-hero { align-items: flex-start; flex-direction: column; }
  .hero-actions { width: 100%; }
  .hero-action { flex: 1; }
  .hero-action :deep(.el-button) { width: 100%; }
  .notice-row { flex-direction: column; gap: 2px; }
  .post-tags { margin-left: auto; justify-content: flex-end; }
  .post-meta { flex-wrap: wrap; gap: 8px; }
  .post-card-header .avatar-sm { flex: 0 0 auto; }
  .fish-feed-card { padding: 15px; }
  .mobile-quick-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; margin-bottom: 14px; }
}

/* 同时支持手动切换到手机模式（PC 大屏 + view=mobile 时也能看到） */
.view-mobile .mobile-quick-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; margin-bottom: 14px; }
</style>
