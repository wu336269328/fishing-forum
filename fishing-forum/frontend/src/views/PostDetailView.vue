<template>
  <div v-if="post" class="detail-grid responsive-grid page-shell">
    <!-- 主内容区 -->
    <div class="main-col">
      <div class="card post-detail-card">
        <div class="card-header post-author-header">
          <img :src="post.authorAvatar||'/default-avatar.png'" class="avatar-md author-avatar-link" @click="$router.push(`/profile/${post.userId}`)" loading="lazy" />
          <div>
            <span class="text-link" @click="$router.push(`/profile/${post.userId}`)">{{ post.authorName }}</span>
            <div class="text-muted">{{ post.sectionName }} · {{ formatTime(post.createdAt) }} · {{ post.viewCount }} 次浏览</div>
          </div>
        </div>
        <h1 class="detail-title">
          <el-tag v-if="post.postType==='CATCH'" size="small" type="success" class="title-tag">渔获</el-tag>
          <el-tag v-if="post.postType==='REVIEW'" size="small" type="warning" class="title-tag">测评</el-tag>
          {{ post.title }}
        </h1>

        <!-- 渔获信息卡 -->
        <div v-if="catchRecord" class="meta-card catch-card">
          <div class="meta-grid">
            <div v-if="catchRecord.fishSpecies"><span class="meta-label">鱼种</span>{{ catchRecord.fishSpecies }}</div>
            <div v-if="catchRecord.weight"><span class="meta-label">重量</span>{{ catchRecord.weight }} 斤</div>
            <div v-if="catchRecord.bait"><span class="meta-label">饵料</span>{{ catchRecord.bait }}</div>
            <div v-if="catchRecord.spotName"><span class="meta-label">钓点</span>{{ catchRecord.spotName }}</div>
            <div v-if="catchRecord.weather"><span class="meta-label">天气</span>{{ catchRecord.weather }}</div>
            <div v-if="catchRecord.fishingDate"><span class="meta-label">日期</span>{{ catchRecord.fishingDate }}</div>
          </div>
        </div>

        <!-- 装备测评信息卡 -->
        <div v-if="gearReview" class="meta-card review-card">
          <div class="meta-grid">
            <div v-if="gearReview.brand"><span class="meta-label">品牌</span>{{ gearReview.brand }}</div>
            <div v-if="gearReview.model"><span class="meta-label">型号</span>{{ gearReview.model }}</div>
            <div v-if="gearReview.gearCategory"><span class="meta-label">分类</span>{{ gearReview.gearCategory }}</div>
            <div v-if="gearReview.price"><span class="meta-label">价格</span>￥{{ gearReview.price }}</div>
            <div v-if="gearReview.rating"><span class="meta-label">评分</span>{{ gearReview.rating }}/5</div>
          </div>
          <div v-if="gearReview.pros" class="review-pros"><span>优点：</span>{{ gearReview.pros }}</div>
          <div v-if="gearReview.cons" class="review-cons"><span>缺点：</span>{{ gearReview.cons }}</div>
        </div>

        <div class="post-body" v-html="sanitizedContent"></div>
        <div class="action-bar post-action-bar">
          <el-button size="small" :type="post.liked?'primary':''" @click="toggleLike">点赞 {{ post.likeCount }}</el-button>
          <el-button size="small" :type="post.favorited?'warning':''" @click="toggleFavorite">{{ post.favorited?'已收藏':'收藏' }}</el-button>
          <el-button class="weak-action" size="small" @click="showReport=true">举报</el-button>
          <el-button v-if="isOwner" size="small" @click="$router.push(`/post/edit/${post.id}`)">编辑</el-button>
          <el-button v-if="isOwner||isAdmin" size="small" type="danger" @click="deletePost">删除</el-button>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-card card">
        <h3 class="comment-title">评论 ({{ post.commentCount }})</h3>
        <div v-if="userStore.isLoggedIn" class="comment-editor">
          <img :src="userStore.user?.avatar||'/default-avatar.png'" class="avatar-sm" style="margin-top:4px" />
          <div class="comment-input-wrap">
            <el-input v-model="commentContent" class="compact-comment-input" type="textarea" :rows="2" placeholder="说点什么..." />
            <el-button class="comment-submit" type="primary" size="small" @click="submitComment(null)" :disabled="!commentContent.trim()">发表</el-button>
          </div>
        </div>
        <div v-else class="login-tip"><router-link to="/login">登录</router-link>后参与评论</div>

          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-main">
              <img :src="c.authorAvatar||'/default-avatar.png'" class="avatar-sm" />
              <div class="comment-body">
                <div class="comment-meta">
                  <span class="text-link">{{ c.authorName }}</span>
                  <span>{{ formatTime(c.createdAt) }}</span>
                </div>
                <p>{{ c.content }}</p>
                <div class="comment-tools">
                  <el-button v-if="userStore.isLoggedIn" text size="small" @click="replyTarget=c">回复</el-button>
                </div>
                <div v-if="replyTarget?.id===c.id" class="reply-editor">
                  <el-input v-model="replyContent" placeholder="回复..." size="small" />
                  <el-button size="small" type="primary" @click="submitComment(c.id)">回复</el-button>
                  <el-button size="small" @click="replyTarget=null">取消</el-button>
                </div>
                <div v-if="c.children?.length" class="child-comment-list">
                  <div v-for="sc in c.children" :key="sc.id" class="child-comment">
                    <div class="comment-meta">
                      <img :src="sc.authorAvatar||'/default-avatar.png'" class="child-avatar" />
                      <span class="text-link">{{ sc.authorName }}</span>
                      <span>{{ formatTime(sc.createdAt) }}</span>
                    </div>
                    <p>{{ sc.content }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        <el-empty v-if="!comments.length" class="compact-empty" description="暂无评论，来抢沙发" :image-size="40" />
      </div>
    </div>

    <!-- 侧边栏 -->
    <div class="side-col">
      <div
        class="card side-author-card"
        role="button"
        tabindex="0"
        @click="$router.push(`/profile/${post.userId}`)"
        @keydown.enter.space.prevent="$router.push(`/profile/${post.userId}`)"
      >
        <div class="side-author-row">
          <img :src="post.authorAvatar||'/default-avatar.png'" class="avatar-lg" loading="lazy" />
          <div>
            <div class="side-author-name">{{ post.authorName }}</div>
            <div class="text-muted side-author-bio">{{ post.authorBio || '这个人很懒，什么都没写' }}</div>
          </div>
        </div>
      </div>
      <div class="card">
        <h3 class="side-section-title">帖子数据</h3>
        <div class="side-stat"><span>浏览</span><b>{{ post.viewCount }}</b></div>
        <div class="side-stat"><span>点赞</span><b>{{ post.likeCount }}</b></div>
        <div class="side-stat"><span>评论</span><b>{{ post.commentCount }}</b></div>
        <div class="side-stat"><span>板块</span><b>{{ post.sectionName }}</b></div>
        <div class="side-stat" v-if="post.tagName"><span>标签</span><b>#{{ post.tagName }}</b></div>
      </div>
      <div class="card" v-if="hotPosts.length">
        <h3 class="side-section-title">热门帖子</h3>
        <div
          v-for="(hp, i) in hotPosts"
          :key="hp.id"
          class="side-item"
          role="button"
          tabindex="0"
          @click="$router.push(`/post/${hp.id}`)"
          @keydown.enter.space.prevent="$router.push(`/post/${hp.id}`)"
        >
          <span class="hot-rank" :class="'rank-'+(i+1)">{{ i+1 }}</span>
          <span class="side-post-title">{{ hp.title }}</span>
        </div>
      </div>
      <div class="card">
        <h3 class="side-section-title">快捷入口</h3>
        <router-link to="/forum" class="quick-link">论坛首页</router-link>
        <router-link to="/post/create" class="quick-link">发表帖子</router-link>
        <router-link to="/spots" class="quick-link">钓点推荐</router-link>
        <router-link to="/wiki" class="quick-link">钓鱼百科</router-link>
      </div>
    </div>

    <el-dialog v-model="showReport" title="举报" width="min(92vw, 420px)" class="responsive-dialog">
      <el-input v-model="reportReason" type="textarea" :rows="3" placeholder="举报原因" />
      <template #footer><el-button @click="showReport=false">取消</el-button><el-button type="primary" @click="submitReport">提交</el-button></template>
    </el-dialog>
  </div>
  <div v-else-if="loadError" class="page-shell">
    <div class="card desktop-error-card">
      <div>
        <b>帖子加载失败</b>
        <p class="text-muted">当前无法获取帖子内容，请检查后端服务或稍后重试。</p>
      </div>
      <el-button size="small" type="primary" @click="loadPostDetail">重新加载</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import DOMPurify from 'dompurify'

const route = useRoute(), router = useRouter(), userStore = useUserStore()
const post = ref(null), comments = ref([]), commentContent = ref(''), replyContent = ref(''), replyTarget = ref(null), showReport = ref(false), reportReason = ref('')
const loadError = ref(false)
const catchRecord = ref(null), gearReview = ref(null), hotPosts = ref([])
const isOwner = computed(() => post.value && userStore.userId === post.value.userId)
const isAdmin = computed(() => userStore.isAdmin)

// XSS安全：用DOMPurify过滤帖子HTML内容
const sanitizedContent = computed(() => post.value ? DOMPurify.sanitize(post.value.content) : '')

const formatTime = (t) => { if (!t) return ''; const d = new Date(t), now = new Date(), diff = (now - d) / 1000; if (diff < 60) return '刚刚'; if (diff < 3600) return Math.floor(diff / 60) + '分钟前'; if (diff < 86400) return Math.floor(diff / 3600) + '小时前'; return d.toLocaleDateString('zh-CN') }

const requireLogin = (action) => {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); router.push('/login'); return false }
  return true
}

const loadComments = async (postId) => {
  const c = await request.get(`/api/comments/${postId}`)
  if (c.code === 200) comments.value = c.data || []
}

const loadHotPosts = async (postId) => {
  const hp = await request.get('/api/posts/hot', { params: { limit: 8 } })
  if (hp.code === 200) hotPosts.value = (hp.data || []).filter(p => p.id !== Number(postId))
}

const loadPostDetail = async () => {
  const postId = route.params.id
  loadError.value = false
  post.value = null
  comments.value = []
  catchRecord.value = null
  gearReview.value = null
  commentContent.value = ''
  replyContent.value = ''
  replyTarget.value = null
  showReport.value = false
  reportReason.value = ''

  try {
    const r = await request.get(`/api/posts/${postId}`)
    if (r.code === 200) {
      post.value = r.data.post || r.data
      catchRecord.value = r.data.catchRecord || null
      gearReview.value = r.data.gearReview || null
    }
    await loadComments(postId)
    await loadHotPosts(postId)
  } catch (e) {
    loadError.value = true
  }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(loadPostDetail)
watch(() => route.params.id, loadPostDetail)

const submitComment = async (parentId) => {
  if (!requireLogin()) return
  const content = parentId ? replyContent.value : commentContent.value
  if (!content.trim()) return
  const r = await request.post('/api/comments', { postId: post.value.id, content, parentId })
  if (r.code === 200) { ElMessage.success('评论成功'); commentContent.value = ''; replyContent.value = ''; replyTarget.value = null; await loadComments(route.params.id); post.value.commentCount++ }
}
const toggleLike = async () => {
  if (!requireLogin()) return
  const r = await request.post('/api/likes', { targetId: post.value.id, targetType: 'POST' }); if (r.code === 200) { post.value.liked = !post.value.liked; post.value.likeCount += post.value.liked ? 1 : -1 }
}
const toggleFavorite = async () => {
  if (!requireLogin()) return
  const r = await request.post('/api/favorites', { postId: post.value.id }); if (r.code === 200) { post.value.favorited = !post.value.favorited; ElMessage.success(r.message) }
}
const submitReport = async () => {
  if (!requireLogin()) return
  if (!reportReason.value.trim()) return ElMessage.warning('请填写原因'); await request.post('/api/reports', { targetId: post.value.id, targetType: 'POST', reason: reportReason.value }); showReport.value = false; reportReason.value = ''; ElMessage.success('已提交')
}
const deletePost = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？', '删除确认', { type: 'warning' })
    const r = await request.delete(`/api/posts/${post.value.id}`)
    if (r.code === 200) { ElMessage.success('已删除'); router.push('/forum') }
    else ElMessage.error(r.message)
  } catch (e) { /* cancelled */ }
}
</script>

<style scoped>
.author-avatar-link { cursor: pointer; }
.login-tip { text-align: center; padding: 10px; font-size: 13px; color: var(--muted); background: var(--surface-muted); border-radius: 12px; }
.side-section-title { font-size: 14px; font-weight: 700; margin-bottom: 10px; color: var(--ink); }
.side-author-card { cursor: pointer; transition: box-shadow .15s, border-color .15s; }
.side-author-card:hover { border-color: var(--color-primary); box-shadow: var(--shadow-hover); }
.side-author-row { display: flex; gap: 12px; align-items: center; }
.side-author-name { font-weight: 700; font-size: 15px; color: var(--ink); }
.side-author-bio { margin-top: 4px; }
.detail-grid { grid-template-columns: minmax(0, 3fr) 1fr; }
.main-col { min-width: 0; }
.detail-title { font-size: clamp(22px, 3vw, 34px); line-height: 1.22; margin-bottom: 14px; color: var(--ink); letter-spacing: -0.03em; }
.post-author-header { margin-bottom: 12px; }
.title-tag { margin-right: 6px; }
.post-detail-card { border-color: var(--border-subtle); }
.post-body { line-height: 1.78; font-size: 15px; color: #2f3a48; }
.post-body :deep(p) { margin: 0 0 12px; }
.post-body :deep(img) { max-width: 100%; border-radius: 4px; }
.action-bar { margin-top: 16px; padding-top: 12px; border-top: 1px solid var(--line); display: flex; gap: 8px; flex-wrap: wrap; }
.post-action-bar .el-button { border-radius: 999px !important; min-height: 36px; padding: 8px 14px; }
.post-action-bar .weak-action { color: var(--muted); background: var(--surface-muted); border-color: var(--border-subtle); }
.comment-card { border-top: 3px solid #dceaf7; }
.comment-title { font-size: 15px; font-weight: 700; margin-bottom: 10px; color: var(--ink); }
.comment-editor { margin-bottom: 14px; display: flex; gap: 10px; align-items: flex-start; }
.comment-input-wrap { flex: 1; min-width: 0; }
.compact-comment-input :deep(textarea) { min-height: 52px !important; max-height: 64px; }
.comment-submit { margin-top: 6px; border-radius: 999px !important; }
.comment-item { padding: 12px 0; border-bottom: 1px solid var(--border-subtle); }
.comment-main { display: flex; gap: 10px; align-items: flex-start; }
.comment-body { flex: 1; min-width: 0; }
.comment-body p { margin: 6px 0; color: #2f3a48; font-size: 14px; line-height: 1.65; white-space: pre-wrap; word-break: break-word; }
.comment-meta { display: flex; align-items: center; gap: 8px; color: var(--muted); font-size: 12px; }
.comment-tools { display: flex; gap: 4px; flex-wrap: wrap; }
.reply-editor { display: flex; gap: 6px; margin-top: 8px; }
.child-comment-list { margin-top: 8px; padding: 8px 10px; border-radius: 14px; background: var(--surface-muted); display: grid; gap: 8px; }
.child-comment p { margin-left: 28px; font-size: 13px; }
.child-avatar { width: 22px; height: 22px; border-radius: 50%; object-fit: cover; }
.compact-empty { height: 120px; display: flex; align-items: center; justify-content: center; }
.compact-empty :deep(.el-empty__description) { margin-top: 4px; }
.meta-card { border-radius: 12px; padding: 12px 16px; margin-bottom: 12px; font-size: 13px; }
.catch-card { background: var(--color-success-soft); border: 1px solid #cbe9dc; }
.review-card { background: var(--color-warning-soft); border: 1px solid #f3d29d; }
.meta-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 6px 16px; }
.meta-label { font-weight: 600; margin-right: 4px; color: var(--muted); }
.review-pros, .review-cons { margin-top: 6px; font-size: 13px; }
.review-pros span { color: var(--color-success); font-weight: 700; }
.review-cons span { color: var(--color-danger); font-weight: 700; }
.side-stat { display: flex; justify-content: space-between; padding: 6px 0; font-size: 13px; border-bottom: 1px solid var(--border-subtle); color: var(--ink); }
.side-stat:last-child { border-bottom: none; }
.side-stat span { color: var(--muted); }
.side-item { display: flex; align-items: center; gap: 8px; padding: 6px 0; font-size: 13px; border-radius: 8px; }
.side-item:hover { background: var(--color-primary-soft); padding-left: 6px; padding-right: 6px; }
.hot-rank { width: 20px; height: 20px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 800; background: #eef2f7; color: var(--muted); flex-shrink: 0; }
.rank-1 { background: var(--rank-1); color: #fff; }
.rank-2 { background: var(--rank-2); color: #fff; }
.rank-3 { background: var(--rank-3); color: #fff; }
.quick-link { display: block; padding: 6px 0; font-size: 13px; color: var(--ink); border-radius: 6px; }
.quick-link:hover { color: var(--color-primary); padding-left: 4px; }
@media (max-width: 900px) {
  .detail-grid { grid-template-columns: 1fr; }
  .side-col { display: none; }
  .meta-grid { grid-template-columns: 1fr; }
  .detail-title { font-size: 23px; line-height: 1.28; letter-spacing: -0.02em; }
  .post-body { font-size: 15px; line-height: 1.75; }
  .post-action-bar { gap: 8px; }
  .post-action-bar .el-button { flex: 0 0 auto; }
  .reply-editor { flex-wrap: wrap; }
  .reply-editor .el-input { flex: 1 1 100%; }
  .child-comment-list { margin-left: 0; }
  .comment-editor .avatar-sm { display: none; }
}
</style>
