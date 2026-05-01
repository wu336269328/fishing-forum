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
          :class="{ 'has-thumb': !!getPostThumbnail(post), 'is-pinned': post.isTop, 'is-featured': post.isFeatured }"
          role="button"
          tabindex="0"
          @click="$router.push(`/post/${post.id}`)"
          @keydown.enter.space.prevent="$router.push(`/post/${post.id}`)"
        >
          <header class="post-author-row">
            <img :src="post.authorAvatar||'/default-avatar.png'" class="avatar-sm" loading="lazy" alt="" />
            <div class="post-author-block">
              <span class="text-link author-name" @click.stop="$router.push(`/profile/${post.userId}`)">{{ post.authorName }}</span>
              <span class="post-date">{{ formatTime(post.createdAt) }} · {{ post.sectionName }}</span>
            </div>
            <div class="post-flags">
              <span v-if="post.isTop" class="ui-chip ui-chip--danger">置顶</span>
              <span v-if="post.isFeatured" class="ui-chip ui-chip--warning">精华</span>
              <span v-if="post.postType==='CATCH'" class="ui-chip ui-chip--success">渔获</span>
              <span v-if="post.postType==='REVIEW'" class="ui-chip ui-chip--section">测评</span>
            </div>
          </header>
          <div class="post-card-main">
            <div class="post-card-copy">
              <h3 class="post-title">{{ post.title }}</h3>
              <p class="post-excerpt">{{ stripHtml(post.content) }}</p>
              <div class="fish-tags" v-if="post.tagName">
                <span class="ui-chip">#{{ post.tagName }}</span>
              </div>
            </div>
            <div v-if="getPostThumbnail(post)" class="post-thumb-wrap">
              <img
                :src="getPostThumbnail(post)"
                class="post-thumb"
                alt=""
                loading="lazy"
                @error="hideBrokenThumbnail"
              />
            </div>
          </div>
          <footer class="post-stats">
            <span class="stat-item-meta"><i class="stat-icon">👁</i>{{ post.viewCount || 0 }}</span>
            <span class="stat-item-meta"><i class="stat-icon">💬</i>{{ post.commentCount || 0 }}</span>
            <span class="stat-item-meta"><i class="stat-icon">👍</i>{{ post.likeCount || 0 }}</span>
          </footer>
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
import { getPostThumbnail, hideBrokenThumbnail } from '../utils/postThumbnail'

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
/* 帖子卡片 - 与首页一致的 PC 优先版面 */
.post-item {
  position: relative;
  cursor: pointer;
  border-color: var(--border-subtle);
  box-shadow: var(--shadow-card);
  padding: 22px 24px;
  margin-bottom: 16px;
  transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease;
}
.post-item:hover {
  transform: translateY(-2px);
  border-color: var(--color-primary);
  box-shadow: var(--shadow-hover);
}
.post-item.is-pinned::before,
.post-item.is-featured::before {
  content: "";
  position: absolute;
  left: -1px; top: 14px; bottom: 14px;
  width: 4px;
  border-radius: 0 4px 4px 0;
}
.post-item.is-pinned::before { background: var(--color-danger); }
.post-item.is-featured:not(.is-pinned)::before { background: var(--color-warning); }

.post-author-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.post-author-row .avatar-sm { width: 36px; height: 36px; flex: 0 0 36px; }
.post-author-block { display: flex; flex-direction: column; min-width: 0; flex: 1; gap: 2px; }
.author-name { font-size: 14px; font-weight: 700; color: var(--ink); }
.post-date { color: var(--muted-soft); font-size: 12px; line-height: 1.3; }
.post-flags { display: flex; gap: 5px; flex-wrap: wrap; flex-shrink: 0; }

.post-card-main { display: flex; align-items: stretch; gap: 22px; min-width: 0; margin-bottom: 14px; }
.post-card-copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 10px; }
.post-card-copy .fish-tags { margin-top: auto; }

.post-thumb-wrap {
  flex: 0 0 clamp(240px, 24vw, 360px);
  width: clamp(240px, 24vw, 360px);
  align-self: stretch;
  min-height: 180px;
  border-radius: 14px;
  overflow: hidden;
  background: var(--surface-muted);
  border: 1px solid var(--border-subtle);
  aspect-ratio: 3 / 2;
  position: relative;
}
.post-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform .4s ease;
}
.post-item:hover .post-thumb { transform: scale(1.04); }

.post-title {
  font-size: 19px;
  font-weight: 800;
  margin: 0;
  color: var(--ink);
  line-height: 1.4;
  letter-spacing: -0.015em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
.post-excerpt {
  font-size: 14px;
  color: var(--muted);
  margin: 0;
  line-height: 1.65;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.fish-tags { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; margin: 0; }

.post-stats {
  display: flex;
  gap: 22px;
  padding-top: 12px;
  border-top: 1px solid var(--border-subtle);
  font-size: 13px;
  color: var(--muted);
}
.stat-item-meta { display: inline-flex; align-items: center; gap: 5px; font-weight: 500; }
.stat-icon { font-style: normal; font-size: 14px; }
@media (max-width: 900px) {
  .post-thumb-wrap { flex: 0 0 200px; width: 200px; }
}

@media (max-width: 768px) {
  .forum-filter-panel { display: grid; gap: 10px; }
  .filter-block { width: 100%; }
  .filter-actions { width: 100%; margin-left: 0; display: grid; grid-template-columns: 1fr 112px; }
  .filter-actions .el-input, .filter-actions .el-select { width: 100%; }
  .filter-actions .el-button { width: 100%; }
  .post-item { padding: 14px 16px; }
  .post-card-main { gap: 12px; }
  .post-thumb-wrap { flex: 0 0 160px; width: 160px; }
  .post-stats { gap: 14px; }
  .forum-layout { grid-template-columns: 1fr; }
  .forum-aside { display: none; }
}

@media (max-width: 540px) {
  .post-card-main { flex-direction: column; gap: 12px; }
  .post-thumb-wrap { flex: 0 0 auto; width: 100%; aspect-ratio: 16 / 9; max-height: 220px; border-radius: 12px; }
  .post-card-copy { gap: 6px; }
  .post-title { font-size: 16px; }
}
</style>
