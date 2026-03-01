<template>
  <div>
    <h1 class="page-title">论坛</h1>
    <!-- 筛选 -->
    <div class="card filter-bar">
      <el-tag v-for="s in [{id:null,name:'全部',icon:''},...sections]" :key="s.id||'all'" :effect="selectedSection===s.id?'dark':'plain'"
              @click="selectedSection=s.id" style="cursor:pointer; margin:2px">{{ s.icon||'📋' }} {{ s.name }}</el-tag>
      <div style="margin-left:auto; display:flex; gap:8px; flex-shrink:0">
        <el-input v-model="keyword" placeholder="搜索帖子..." clearable size="small" style="width:160px" @keyup.enter="loadPosts" />
        <el-select v-model="sortBy" size="small" style="width:100px">
          <el-option label="最新" value="latest" /><el-option label="最热" value="hot" /><el-option label="点赞" value="likes" />
        </el-select>
      </div>
    </div>
    <!-- 帖子 -->
    <div v-for="post in posts" :key="post.id" class="card post-item" @click="$router.push(`/post/${post.id}`)">
      <div class="card-header">
        <img :src="post.authorAvatar||'/default-avatar.png'" class="avatar-sm" />
        <div>
          <span class="text-link" @click.stop="$router.push(`/profile/${post.userId}`)">{{ post.authorName }}</span>
          <span class="text-muted" style="margin-left:6px">{{ formatTime(post.createdAt) }}</span>
        </div>
        <div style="margin-left:auto; display:flex; gap:4px">
          <el-tag v-if="post.isTop" size="small" type="danger">置顶</el-tag>
          <el-tag v-if="post.isFeatured" size="small" type="warning">精华</el-tag>
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
import request from '../api/request'

const route = useRoute()
const posts = ref([]), sections = ref([]), selectedSection = ref(null), keyword = ref(''), sortBy = ref('latest'), currentPage = ref(1), total = ref(0)

const formatTime = (t) => { if (!t) return ''; const d=new Date(t),now=new Date(),diff=(now-d)/1000; if(diff<60) return '刚刚'; if(diff<3600) return Math.floor(diff/60)+'分钟前'; if(diff<86400) return Math.floor(diff/3600)+'小时前'; return d.toLocaleDateString('zh-CN') }
const stripHtml = (h) => h ? h.replace(/<[^>]+>/g,'').substring(0,120) : ''

const loadPosts = async () => {
  const res = await request.get('/api/posts', { params: { page: currentPage.value, size: 10, sectionId: selectedSection.value, keyword: keyword.value, sort: sortBy.value } })
  if (res.code === 200) { posts.value = res.data.records || []; total.value = res.data.total || 0 }
}
watch([selectedSection, sortBy], () => { currentPage.value = 1; loadPosts() })
onMounted(async () => {
  if (route.query.sectionId) selectedSection.value = Number(route.query.sectionId)
  const r = await request.get('/api/sections'); if (r.code === 200) sections.value = r.data || []
  loadPosts()
})
</script>

<style scoped>
.filter-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 2px; }
.post-item { cursor: pointer; transition: box-shadow 0.15s; }
.post-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.post-title { font-size: 15px; font-weight: 600; margin-bottom: 4px; color: #222; }
.post-excerpt { font-size: 13px; color: #777; margin-bottom: 8px; line-height: 1.5; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.post-meta { font-size: 12px; color: #999; display: flex; gap: 12px; }
</style>
