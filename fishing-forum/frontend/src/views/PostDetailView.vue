<template>
  <div v-if="post">
    <div class="card">
      <div class="card-header" style="margin-bottom:12px">
        <img :src="post.authorAvatar||'/default-avatar.png'" class="avatar-md" @click="$router.push(`/profile/${post.userId}`)" style="cursor:pointer" />
        <div>
          <span class="text-link" @click="$router.push(`/profile/${post.userId}`)">{{ post.authorName }}</span>
          <div class="text-muted">{{ post.sectionName }} · {{ formatTime(post.createdAt) }} · {{ post.viewCount }} 次浏览</div>
        </div>
      </div>
      <h1 style="font-size:20px; margin-bottom:12px">{{ post.title }}</h1>
      <div class="post-body" v-html="sanitizedContent"></div>
      <div style="margin-top:16px; padding-top:12px; border-top:1px solid #eee; display:flex; gap:8px">
        <el-button size="small" :type="post.liked?'primary':''" @click="toggleLike">👍 {{ post.likeCount }}</el-button>
        <el-button size="small" :type="post.favorited?'warning':''" @click="toggleFavorite">{{ post.favorited?'⭐ 已收藏':'☆ 收藏' }}</el-button>
        <el-button size="small" @click="showReport=true">🚩 举报</el-button>
        <el-button v-if="isOwner" size="small" @click="$router.push(`/post/edit/${post.id}`)">✏️ 编辑</el-button>
        <el-button v-if="isOwner||isAdmin" size="small" type="danger" @click="deletePost">🗑 删除</el-button>
      </div>
    </div>

    <!-- 评论区 -->
    <div class="card">
      <h3 style="font-size:15px; margin-bottom:12px">💬 评论 ({{ post.commentCount }})</h3>
      <div v-if="userStore.isLoggedIn" style="margin-bottom:16px; display:flex; gap:10px">
        <img :src="userStore.user?.avatar||'/default-avatar.png'" class="avatar-sm" style="margin-top:4px" />
        <div style="flex:1">
          <el-input v-model="commentContent" type="textarea" :rows="2" placeholder="说点什么..." />
          <el-button type="primary" size="small" style="margin-top:6px" @click="submitComment(null)" :disabled="!commentContent.trim()">发表</el-button>
        </div>
      </div>
      <div v-else style="text-align:center; padding:8px; font-size:13px; color:#999"><router-link to="/login">登录</router-link>后参与评论</div>

      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="card-header" style="margin-bottom:4px">
          <img :src="c.authorAvatar||'/default-avatar.png'" class="avatar-sm" />
          <span class="text-link" style="font-size:13px">{{ c.authorName }}</span>
          <span class="text-muted">{{ formatTime(c.createdAt) }}</span>
          <span v-if="userStore.isLoggedIn" class="text-muted text-link" style="margin-left:auto" @click="replyTarget=c">回复</span>
        </div>
        <p style="font-size:14px; padding-left:40px; margin-bottom:4px">{{ c.content }}</p>
        <div v-if="replyTarget?.id===c.id" style="display:flex; gap:6px; margin:6px 0 0 40px">
          <el-input v-model="replyContent" placeholder="回复..." size="small" />
          <el-button size="small" type="primary" @click="submitComment(c.id)">回复</el-button>
          <el-button size="small" @click="replyTarget=null">取消</el-button>
        </div>
        <div v-if="c.children?.length" style="margin:8px 0 0 40px; padding-left:12px; border-left:2px solid #eee">
          <div v-for="sc in c.children" :key="sc.id" style="margin-bottom:6px">
            <div style="display:flex; gap:6px; align-items:center; font-size:12px; color:#999">
              <img :src="sc.authorAvatar||'/default-avatar.png'" style="width:20px;height:20px;border-radius:50%" />
              <span class="text-link">{{ sc.authorName }}</span>
              <span>{{ formatTime(sc.createdAt) }}</span>
            </div>
            <p style="font-size:13px; padding-left:26px">{{ sc.content }}</p>
          </div>
        </div>
      </div>
      <el-empty v-if="!comments.length" description="暂无评论，来说两句" :image-size="40" />
    </div>

    <el-dialog v-model="showReport" title="举报" width="380px">
      <el-input v-model="reportReason" type="textarea" :rows="3" placeholder="举报原因" />
      <template #footer><el-button @click="showReport=false">取消</el-button><el-button type="primary" @click="submitReport">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import DOMPurify from 'dompurify'

const route = useRoute(), router = useRouter(), userStore = useUserStore()
const post = ref(null), comments = ref([]), commentContent = ref(''), replyContent = ref(''), replyTarget = ref(null), showReport = ref(false), reportReason = ref('')
const isOwner = computed(() => post.value && userStore.userId === post.value.userId)
const isAdmin = computed(() => userStore.isAdmin)

// XSS安全：用DOMPurify过滤帖子HTML内容
const sanitizedContent = computed(() => post.value ? DOMPurify.sanitize(post.value.content) : '')

const formatTime = (t) => { if (!t) return ''; const d = new Date(t), now = new Date(), diff = (now - d) / 1000; if (diff < 60) return '刚刚'; if (diff < 3600) return Math.floor(diff / 60) + '分钟前'; if (diff < 86400) return Math.floor(diff / 3600) + '小时前'; return d.toLocaleDateString('zh-CN') }

const requireLogin = (action) => {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); router.push('/login'); return false }
  return true
}

onMounted(async () => {
  const r = await request.get(`/api/posts/${route.params.id}`); if (r.code === 200) post.value = r.data
  const c = await request.get(`/api/comments/${route.params.id}`); if (c.code === 200) comments.value = c.data || []
})

const submitComment = async (parentId) => {
  if (!requireLogin()) return
  const content = parentId ? replyContent.value : commentContent.value
  if (!content.trim()) return
  const r = await request.post('/api/comments', { postId: post.value.id, content, parentId })
  if (r.code === 200) { ElMessage.success('评论成功'); commentContent.value = ''; replyContent.value = ''; replyTarget.value = null; const c = await request.get(`/api/comments/${route.params.id}`); if (c.code === 200) comments.value = c.data || []; post.value.commentCount++ }
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
.post-body { line-height: 1.8; font-size: 15px; color: #444; }
.post-body :deep(img) { max-width: 100%; border-radius: 4px; }
.comment-item { padding: 10px 0; border-bottom: 1px solid #f5f5f5; }
</style>
