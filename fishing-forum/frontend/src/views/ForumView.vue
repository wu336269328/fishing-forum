<template>
  <div class="page-shell">
    <section class="hero-panel">
      <p class="eyebrow">Forum</p>
      <h1 class="hero-title">讨论、渔获、装备测评都在这里。</h1>
      <p class="hero-subtitle">用板块、标签和排序快速找到对你有用的钓鱼经验。</p>
    </section>
    <!-- 筛选 -->
    <div class="page-toolbar forum-filter-panel">
      <div class="filter-block">
        <span class="filter-label">类型</span>
        <div class="filter-chip-row">
          <button v-for="t in postTypes" :key="t.value" type="button" class="ui-chip" :class="{ active: selectedType===t.value }" @click="selectedType=t.value">{{ t.label }}</button>
        </div>
      </div>
      <div class="filter-block">
        <span class="filter-label">板块</span>
        <div class="filter-chip-row">
          <button v-for="s in [{id:null,name:'全部'},...sections]" :key="s.id||'all'" type="button" class="ui-chip" :class="{ active: selectedSection===s.id }" @click="selectedSection=s.id; selectedTag=null">{{ s.name }}</button>
        </div>
      </div>
      <div class="filter-actions">
        <el-input v-model="keyword" placeholder="搜索帖子" clearable size="small" @keyup.enter="loadPosts" />
        <el-select v-model="sortBy" size="small">
          <el-option label="最新" value="latest" /><el-option label="最热" value="hot" /><el-option label="点赞" value="likes" />
        </el-select>
        <el-button size="small" type="primary" @click="loadPosts">搜索</el-button>
        <el-button size="small" @click="clearFilters">清除</el-button>
      </div>
    </div>
    <!-- 标签筛选 -->
    <div v-if="tags.length" class="page-toolbar tag-filter-panel">
      <span class="filter-label">标签</span>
      <div class="filter-chip-row">
        <button v-for="tag in [{id:null,name:'全部'},...tags]" :key="tag.id||'all'" type="button" class="ui-chip" :class="{ active: selectedTag===tag.id }" @click="selectedTag=tag.id">{{ tag.name }}</button>
      </div>
    </div>
    <div class="forum-layout">
      <div class="forum-main">
        <!-- 帖子 -->
        <article
          v-for="post in posts"
          :key="post.id"
          class="card post-item list-card"
          role="button"
          tabindex="0"
          @click="$router.push(`/post/${post.id}`)"
          @keydown.enter.space.prevent="$router.push(`/post/${post.id}`)"
        >
          <div class="card-header">
            <img :src="post.authorAvatar||'/default-avatar.png'" class="avatar-sm" loading="lazy" />
            <div class="post-author-line">
              <span class="text-link" @click.stop="$router.push(`/profile/${post.userId}`)">{{ post.authorName }}</span>
              <span class="text-muted post-date">{{ formatTime(post.createdAt) }}</span>
            </div>
            <div class="post-tags">
              <el-tag v-if="post.postType==='CATCH'" size="small" type="success">渔获</el-tag>
              <el-tag v-if="post.postType==='REVIEW'" size="small" type="warning">测评</el-tag>
              <el-tag v-if="post.isTop" size="small" type="danger">置顶</el-tag>
              <el-tag v-if="post.isFeatured" size="small" type="warning">精华</el-tag>
              <el-tag v-if="post.tagName" size="small" effect="plain">{{ post.tagName }}</el-tag>
              <el-tag size="small" effect="plain">{{ post.sectionName }}</el-tag>
            </div>
          </div>
          <div class="post-title">{{ post.title }}</div>
          <div class="post-excerpt">{{ stripHtml(post.content) }}</div>
          <div class="post-meta">
            <span>浏览 {{ post.viewCount }}</span>
            <span>评论 {{ post.commentCount }}</span>
            <span>点赞 {{ post.likeCount }}</span>
          </div>
        </article>
        <div v-if="loadError" class="card desktop-error-card">
          <div>
            <b>帖子加载失败</b>
            <p class="text-muted">后端暂时不可用，检查服务后重试。</p>
          </div>
          <el-button size="small" type="primary" @click="loadPosts">重新加载</el-button>
        </div>
        <div v-else-if="!posts.length" class="card empty-state">
          <div class="empty-state-icon">🎣</div>
          <div class="empty-state-title">这里还没有帖子</div>
          <div class="empty-state-text">{{ emptyHint }}</div>
          <el-button type="primary" size="small" round @click="$router.push('/post/create')">发表第一帖</el-button>
        </div>
        <div class="pagination-wrap" v-if="total>10">
          <el-pagination background layout="prev,pager,next" :total="total" :page-size="10" v-model:current-page="currentPage" @current-change="loadPosts" />
        </div>
      </div>
      <div class="forum-aside card">
        <h3>论坛导航</h3>
        <p class="text-muted">PC 版优先使用搜索、排序和板块组合筛选。</p>
        <router-link to="/post/create" class="quick-link">发表帖子</router-link>
        <router-link to="/forum?postType=CATCH" class="quick-link">渔获日记</router-link>
        <router-link to="/forum?postType=REVIEW" class="quick-link">装备测评</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import request from '../api/request'

const route = useRoute()
const userStore = useUserStore()
const posts = ref([]), sections = ref([]), tags = ref([])
const selectedSection = ref(null), selectedTag = ref(null), selectedType = ref('')
const keyword = ref(''), sortBy = ref('latest'), currentPage = ref(1), total = ref(0)
const loadError = ref(false)

const postTypes = [
  { value: '', label: '全部' },
  { value: 'FOLLOWING', label: '关注动态' },
  { value: 'NORMAL', label: '讨论' },
  { value: 'CATCH', label: '渔获日记' },
  { value: 'REVIEW', label: '装备测评' }
]

const emptyHint = computed(() => {
  if (selectedType.value === 'FOLLOWING') return '还没有关注的钓友发新内容，先去发现页认识更多钓友吧。'
  if (selectedType.value === 'CATCH') return '还没人分享渔获，记录你的下一次出钓让大家看看。'
  if (selectedType.value === 'REVIEW') return '还没有装备测评，分享一下你正在用的鱼竿、饵料感受。'
  if (keyword.value) return `没有搜索到「${keyword.value}」相关帖子，换个关键词试试。`
  if (selectedTag.value || selectedSection.value) return '当前条件下没有帖子，清除筛选看看更多内容。'
  return '成为第一位发帖人，分享你的钓鱼故事。'
})

const formatTime = (t) => { if (!t) return ''; const d=new Date(t),now=new Date(),diff=(now-d)/1000; if(diff<60) return '刚刚'; if(diff<3600) return Math.floor(diff/60)+'分钟前'; if(diff<86400) return Math.floor(diff/3600)+'小时前'; return d.toLocaleDateString('zh-CN') }
const stripHtml = (h) => h ? h.replace(/<[^>]+>/g,'').substring(0,120) : ''

const loadPosts = async () => {
  loadError.value = false
  if (selectedType.value === 'FOLLOWING') {
    if (!userStore.isLoggedIn) { posts.value = []; total.value = 0; return }
    try {
      const feed = await request.get('/api/follows/feed', { params: { page: currentPage.value, size: 10 } })
      if (feed.code === 200) { posts.value = feed.data.records || []; total.value = feed.data.total || 0 }
    } catch (e) { loadError.value = true }
    return
  }
  const params = { page: currentPage.value, size: 10, sectionId: selectedSection.value, keyword: keyword.value, sort: sortBy.value }
  if (selectedType.value) params.postType = selectedType.value
  if (selectedTag.value) params.tagId = selectedTag.value
  try {
    const res = await request.get('/api/posts', { params })
    if (res.code === 200) { posts.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch (e) { loadError.value = true }
}
const loadTags = async () => {
  const params = selectedSection.value ? { sectionId: selectedSection.value } : {}
  try {
    const r = await request.get('/api/tags', { params })
    if (r.code === 200) tags.value = r.data || []
  } catch (e) {
    tags.value = []
    loadError.value = true
  }
}
const clearFilters = () => {
  selectedSection.value = null
  selectedTag.value = null
  selectedType.value = ''
  keyword.value = ''
  sortBy.value = 'latest'
  currentPage.value = 1
  loadTags()
  loadPosts()
}

watch([selectedSection, sortBy, selectedType], () => { currentPage.value = 1; loadPosts() })
watch(selectedSection, loadTags)
watch(selectedTag, () => { currentPage.value = 1; loadPosts() })

onMounted(async () => {
  if (route.query.sectionId) selectedSection.value = Number(route.query.sectionId)
  if (route.query.tagId) selectedTag.value = Number(route.query.tagId)
  if (route.query.postType) selectedType.value = route.query.postType
  try {
    const r = await request.get('/api/sections'); if (r.code === 200) sections.value = r.data || []
  } catch (e) {
    sections.value = []
    loadError.value = true
  }
  await loadTags()
  loadPosts()
})
</script>

<style scoped>
.forum-filter-panel { align-items: stretch; }
.filter-block { display: grid; gap: 6px; min-width: 0; flex: 1 1 240px; }
.filter-label { color: var(--muted); font-size: 12px; font-weight: 800; }
.filter-actions { margin-left: auto; display: flex; align-items: end; gap: 8px; flex: 0 0 auto; }
.filter-actions .el-input { width: 200px; }
.filter-actions .el-select { width: 112px; }
.tag-filter-panel { align-items: center; }
.forum-layout { display: grid; grid-template-columns: minmax(0, 1fr) 280px; gap: 16px; align-items: start; }
.forum-main { min-width: 0; }
.forum-aside { position: sticky; top: 82px; border-color: var(--border-subtle); }
.forum-aside h3 { font-size: 15px; font-weight: 800; margin-bottom: 8px; color: var(--ink); }
.forum-aside .quick-link { display: block; padding: 8px 0; color: var(--ink); font-size: 13px; border-radius: 6px; }
.forum-aside .quick-link:hover { color: var(--color-primary); padding-left: 4px; }
.post-item { cursor: pointer; transition: box-shadow 0.15s; border-color: var(--border-subtle); box-shadow: var(--shadow-card); }
.post-item:hover { box-shadow: var(--shadow-hover); }
.card-header { align-items: center; }
.post-author-line { display: flex; flex-direction: column; min-width: 0; }
.post-date { color: var(--muted-soft); line-height: 1.2; }
.post-title { font-size: 18px; font-weight: 800; margin-bottom: 6px; color: var(--ink); line-height: 1.35; letter-spacing: -0.01em; }
.post-excerpt { font-size: 13px; color: var(--muted); margin-bottom: 8px; line-height: 1.5; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.post-meta { padding-top: 10px; border-top: 1px solid var(--border-subtle); font-size: 12px; color: var(--muted-soft); display: flex; gap: 12px; }
.post-tags { margin-left: auto; display: flex; gap: 4px; flex-wrap: wrap; justify-content: flex-end; }
@media (max-width: 768px) {
  .forum-filter-panel { display: grid; gap: 10px; }
  .filter-block { width: 100%; }
  .filter-actions { width: 100%; margin-left: 0; display: grid; grid-template-columns: 1fr 112px; }
  .filter-actions .el-input, .filter-actions .el-select { width: 100%; }
  .filter-actions .el-button { width: 100%; }
  .post-tags { width: 100%; margin-left: 42px; justify-content: flex-start; }
  .post-meta { flex-wrap: wrap; gap: 8px; }
  .forum-layout { grid-template-columns: 1fr; }
  .forum-aside { display: none; }
}
</style>
