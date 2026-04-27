<template>
  <div class="page-shell">
    <section class="hero-panel">
      <p class="eyebrow">Forum</p>
      <h1 class="hero-title">讨论、渔获、装备测评都在这里。</h1>
      <p class="hero-subtitle">用板块、标签和排序快速找到对你有用的钓鱼经验。</p>
    </section>
    <!-- 类型筛选 -->
    <div class="card type-bar">
      <el-tag v-for="t in postTypes" :key="t.value" :effect="selectedType===t.value?'dark':'plain'" @click="selectedType=t.value" style="cursor:pointer">{{ t.icon }} {{ t.label }}</el-tag>
    </div>
    <!-- 板块+标签筛选 -->
    <div class="card filter-bar">
      <el-tag v-for="s in [{id:null,name:'全部',icon:''},...sections]" :key="s.id||'all'" :effect="selectedSection===s.id?'dark':'plain'"
              @click="selectedSection=s.id; selectedTag=null" style="cursor:pointer; margin:2px">{{ s.icon||'📋' }} {{ s.name }}</el-tag>
      <div class="filter-actions">
        <el-input v-model="keyword" placeholder="搜索帖子..." clearable size="small" @keyup.enter="loadPosts" />
        <el-select v-model="sortBy" size="small">
          <el-option label="最新" value="latest" /><el-option label="最热" value="hot" /><el-option label="点赞" value="likes" />
        </el-select>
      </div>
    </div>
    <!-- 标签筛选 -->
    <div v-if="tags.length" class="card" style="padding:8px 16px; display:flex; flex-wrap:wrap; gap:4px">
      <span style="font-size:12px; color:#999; margin-right:4px; line-height:24px">标签:</span>
      <el-tag v-for="tag in [{id:null,name:'全部'},...tags]" :key="tag.id||'all'" size="small"
              :effect="selectedTag===tag.id?'dark':'plain'" :color="tag.id && selectedTag===tag.id ? tag.color : ''"
              @click="selectedTag=tag.id" style="cursor:pointer; margin:1px">{{ tag.name }}</el-tag>
    </div>
    <!-- 帖子 -->
    <div v-for="post in posts" :key="post.id" class="card post-item list-card" @click="$router.push(`/post/${post.id}`)">
      <div class="card-header">
        <img :src="post.authorAvatar||'/default-avatar.png'" class="avatar-sm" />
        <div>
          <span class="text-link" @click.stop="$router.push(`/profile/${post.userId}`)">{{ post.authorName }}</span>
          <span class="text-muted" style="margin-left:6px">{{ formatTime(post.createdAt) }}</span>
        </div>
        <div class="post-tags">
          <el-tag v-if="post.postType==='CATCH'" size="small" type="success">🐟 渔获</el-tag>
          <el-tag v-if="post.postType==='REVIEW'" size="small" type="warning">⭐ 测评</el-tag>
          <el-tag v-if="post.isTop" size="small" type="danger">置顶</el-tag>
          <el-tag v-if="post.isFeatured" size="small" type="warning">精华</el-tag>
          <el-tag v-if="post.tagName" size="small" effect="plain">{{ post.tagName }}</el-tag>
          <el-tag size="small" effect="plain">{{ post.sectionName }}</el-tag>
        </div>
      </div>
      <div class="post-title">{{ post.title }}</div>
      <div class="post-excerpt">{{ stripHtml(post.content) }}</div>
      <div class="post-meta">
        <span>👁 {{ post.viewCount }}</span>
        <span>💬 {{ post.commentCount }}</span>
        <span>👍 {{ post.likeCount }}</span>
      </div>
    </div>
    <el-empty v-if="!posts.length" description="暂无帖子" />
    <div class="pagination-wrap" v-if="total>10">
      <el-pagination background layout="prev,pager,next" :total="total" :page-size="10" v-model:current-page="currentPage" @current-change="loadPosts" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import request from '../api/request'

const route = useRoute()
const userStore = useUserStore()
const posts = ref([]), sections = ref([]), tags = ref([])
const selectedSection = ref(null), selectedTag = ref(null), selectedType = ref('')
const keyword = ref(''), sortBy = ref('latest'), currentPage = ref(1), total = ref(0)

const postTypes = [
  { value: '', label: '全部', icon: '📋' },
  { value: 'FOLLOWING', label: '关注动态', icon: '👀' },
  { value: 'NORMAL', label: '讨论', icon: '💬' },
  { value: 'CATCH', label: '渔获日记', icon: '🐟' },
  { value: 'REVIEW', label: '装备测评', icon: '⭐' }
]

const formatTime = (t) => { if (!t) return ''; const d=new Date(t),now=new Date(),diff=(now-d)/1000; if(diff<60) return '刚刚'; if(diff<3600) return Math.floor(diff/60)+'分钟前'; if(diff<86400) return Math.floor(diff/3600)+'小时前'; return d.toLocaleDateString('zh-CN') }
const stripHtml = (h) => h ? h.replace(/<[^>]+>/g,'').substring(0,120) : ''

const loadPosts = async () => {
  if (selectedType.value === 'FOLLOWING') {
    if (!userStore.isLoggedIn) { posts.value = []; total.value = 0; return }
    const feed = await request.get('/api/follows/feed', { params: { page: currentPage.value, size: 10 } })
    if (feed.code === 200) { posts.value = feed.data.records || []; total.value = feed.data.total || 0 }
    return
  }
  const params = { page: currentPage.value, size: 10, sectionId: selectedSection.value, keyword: keyword.value, sort: sortBy.value }
  if (selectedType.value) params.postType = selectedType.value
  if (selectedTag.value) params.tagId = selectedTag.value
  const res = await request.get('/api/posts', { params })
  if (res.code === 200) { posts.value = res.data.records || []; total.value = res.data.total || 0 }
}
const loadTags = async () => {
  const params = selectedSection.value ? { sectionId: selectedSection.value } : {}
  const r = await request.get('/api/tags', { params })
  if (r.code === 200) tags.value = r.data || []
}

watch([selectedSection, sortBy, selectedType], () => { currentPage.value = 1; loadPosts() })
watch(selectedSection, loadTags)
watch(selectedTag, () => { currentPage.value = 1; loadPosts() })

onMounted(async () => {
  if (route.query.sectionId) selectedSection.value = Number(route.query.sectionId)
  if (route.query.tagId) selectedTag.value = Number(route.query.tagId)
  if (route.query.postType) selectedType.value = route.query.postType
  const r = await request.get('/api/sections'); if (r.code === 200) sections.value = r.data || []
  await loadTags()
  loadPosts()
})
</script>

<style scoped>
.eyebrow { position: relative; z-index: 1; font-size: 12px; color: rgba(255,255,255,.68); text-transform: uppercase; letter-spacing: .12em; margin-bottom: 8px; }
.type-bar { display: flex; gap: 8px; margin-bottom: 0; padding: 10px 16px; overflow-x: auto; }
.filter-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 2px; }
.filter-actions { margin-left: auto; display: flex; gap: 8px; flex-shrink: 0; }
.filter-actions .el-input { width: 180px; }
.filter-actions .el-select { width: 110px; }
.post-item { cursor: pointer; transition: box-shadow 0.15s; }
.post-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.post-title { font-size: 17px; font-weight: 800; margin-bottom: 4px; color: var(--ink); }
.post-excerpt { font-size: 13px; color: #777; margin-bottom: 8px; line-height: 1.5; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.post-meta { font-size: 12px; color: #999; display: flex; gap: 12px; }
.post-tags { margin-left: auto; display: flex; gap: 4px; flex-wrap: wrap; justify-content: flex-end; }
@media (max-width: 768px) {
  .filter-actions { width: 100%; margin-left: 0; }
  .filter-actions .el-input, .filter-actions .el-select { width: 100%; }
  .post-tags { width: 100%; margin-left: 42px; justify-content: flex-start; }
  .post-meta { flex-wrap: wrap; }
}
</style>
