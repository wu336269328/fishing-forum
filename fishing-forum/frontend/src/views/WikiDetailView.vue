<template>
  <div v-if="entry" class="detail-grid responsive-grid page-shell">
    <!-- 主内容区 -->
    <div class="main-col">
      <div class="card">
        <el-button text size="small" @click="$router.push('/wiki')">← 返回百科</el-button>
        <div class="wiki-detail-head">
          <h1>{{ entry.title }}</h1>
          <el-button v-if="userStore.isLoggedIn" size="small" @click="startEdit">编辑词条</el-button>
        </div>
        <div class="wiki-detail-meta">
          <span>{{ entry.category }}</span>
          <span>作者: <span class="text-link" @click="$router.push(`/profile/${entry.userId}`)">{{ entry.authorName }}</span></span>
          <span>版本 v{{ entry.version }}</span>
          <span>浏览 {{ entry.viewCount }}</span>
        </div>
        <div class="wiki-content" v-html="renderedContent"></div>
      </div>

      <!-- 编辑器 -->
      <div v-if="editing" class="card">
        <h3 style="font-size:15px; margin-bottom:12px">编辑词条</h3>
        <el-form label-position="top" size="small">
          <el-form-item label="标题"><el-input v-model="editForm.title" /></el-form-item>
          <el-form-item label="分类">
            <el-select v-model="editForm.category" style="width:100%">
              <el-option v-for="c in ['鱼种','饵料','装备','技巧','常识','鱼种图鉴']" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="内容（Markdown）">
            <el-input v-model="editForm.content" type="textarea" :rows="12" />
          </el-form-item>
          <el-form-item label="插入图片">
            <div style="display:flex; gap:8px; align-items:center">
              <label class="img-upload-btn">
                <input type="file" accept="image/*" style="display:none" @change="uploadWikiImage" />
                {{ wikiUploading ? '上传中...' : '+ 上传图片' }}
              </label>
              <span v-if="lastImgUrl" class="text-muted">已插入: {{ lastImgUrl }}</span>
            </div>
          </el-form-item>
          <div style="display:flex; gap:8px; justify-content:flex-end">
            <el-button @click="editing=false">取消</el-button>
            <el-button type="primary" @click="submitEdit" :loading="saving">保存修改</el-button>
          </div>
        </el-form>
      </div>

      <div class="card">
        <div class="section-head">
          <h3>词条讨论</h3>
          <span class="text-muted">{{ wikiComments.length }} 条讨论</span>
        </div>
        <div v-if="userStore.isLoggedIn" class="comment-editor">
          <el-input v-model="commentContent" type="textarea" :rows="3" maxlength="2000" show-word-limit placeholder="补充经验、指出错误或提问" />
          <div class="comment-actions">
            <el-button type="primary" size="small" @click="submitWikiComment()">发布讨论</el-button>
          </div>
        </div>
        <div v-else class="login-tip">
          <router-link to="/login">登录后参与词条讨论</router-link>
        </div>

        <div v-for="c in wikiComments" :key="c.id" class="comment-item">
          <div class="comment-main">
            <img :src="c.authorAvatar || '/default-avatar.png'" class="comment-avatar" />
            <div class="comment-body">
              <div class="comment-meta">
                <b>{{ c.authorName || '用户' + c.userId }}</b>
                <span>{{ formatTime(c.createdAt) }}</span>
              </div>
              <p>{{ c.content }}</p>
              <div class="comment-tools">
                <el-button text size="small" @click="likeWikiComment(c)">赞 {{ c.likeCount || 0 }}</el-button>
                <el-button v-if="userStore.isLoggedIn" text size="small" @click="replyTarget = replyTarget?.id === c.id ? null : c">回复</el-button>
                <el-button v-if="userStore.isLoggedIn" text size="small" @click="reportWikiComment(c)">举报</el-button>
                <el-button v-if="canDelete(c)" text size="small" type="danger" @click="deleteWikiComment(c)">删除</el-button>
              </div>
              <div v-if="replyTarget?.id === c.id" class="reply-editor">
                <el-input v-model="replyContent" type="textarea" :rows="2" maxlength="2000" placeholder="回复这条讨论" />
                <div class="comment-actions">
                  <el-button size="small" @click="replyTarget = null; replyContent = ''">取消</el-button>
                  <el-button type="primary" size="small" @click="submitWikiComment(c.id)">回复</el-button>
                </div>
              </div>

              <div v-for="child in c.children || []" :key="child.id" class="comment-item child-comment">
                <div class="comment-main">
                  <img :src="child.authorAvatar || '/default-avatar.png'" class="comment-avatar small" />
                  <div class="comment-body">
                    <div class="comment-meta">
                      <b>{{ child.authorName || '用户' + child.userId }}</b>
                      <span>{{ formatTime(child.createdAt) }}</span>
                    </div>
                    <p>{{ child.content }}</p>
                    <div class="comment-tools">
                      <el-button text size="small" @click="likeWikiComment(child)">👍 {{ child.likeCount || 0 }}</el-button>
                      <el-button v-if="userStore.isLoggedIn" text size="small" @click="reportWikiComment(child)">举报</el-button>
                      <el-button v-if="canDelete(child)" text size="small" type="danger" @click="deleteWikiComment(child)">删除</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-if="!wikiComments.length" description="暂无讨论" :image-size="40" />
      </div>
    </div>

    <!-- 侧边栏 -->
    <div class="side-col">
      <!-- 词条信息 -->
      <div class="card">
        <h3 style="font-size:14px; margin-bottom:10px">词条信息</h3>
        <div class="side-stat"><span>分类</span><b>{{ entry.category }}</b></div>
        <div class="side-stat"><span>作者</span><b>{{ entry.authorName }}</b></div>
        <div class="side-stat"><span>版本</span><b>v{{ entry.version }}</b></div>
        <div class="side-stat"><span>浏览</span><b>{{ entry.viewCount }}</b></div>
      </div>

      <!-- 编辑历史 -->
      <div class="card">
        <h3 style="font-size:14px; margin-bottom:10px">编辑历史</h3>
        <div v-for="h in histories" :key="h.id" style="display:flex; gap:8px; padding:5px 0; border-bottom:1px solid #f5f5f5; font-size:12px; align-items:center">
          <el-tag size="small" effect="plain">v{{ h.version }}</el-tag>
          <span style="flex:1">{{ h.authorName }}</span>
          <span class="text-muted">{{ formatTime(h.createdAt) }}</span>
        </div>
        <el-empty v-if="!histories.length" description="暂无" :image-size="24" />
      </div>

      <!-- 同类词条 -->
      <div class="card" v-if="relatedEntries.length">
        <h3 style="font-size:14px; margin-bottom:10px">相关词条</h3>
        <div v-for="re in relatedEntries" :key="re.id" class="side-item" style="cursor:pointer" @click="$router.push(`/wiki/${re.id}`)">
          <span style="font-size:13px; flex:1; overflow:hidden; text-overflow:ellipsis; white-space:nowrap">{{ re.title }}</span>
          <span class="text-muted">{{ re.category }}</span>
        </div>
      </div>

      <!-- 百科分类 -->
      <div class="card">
        <h3 style="font-size:14px; margin-bottom:10px">百科分类</h3>
        <router-link v-for="cat in ['鱼种','饵料','装备','技巧','常识','鱼种图鉴']" :key="cat" :to="`/wiki?category=${cat}`" class="quick-link">{{ cat }}</router-link>
      </div>

      <div class="card">
        <h3 style="font-size:14px; margin-bottom:10px">快捷入口</h3>
        <router-link to="/wiki" class="quick-link">百科首页</router-link>
        <router-link to="/forum" class="quick-link">论坛首页</router-link>
        <router-link to="/spots" class="quick-link">钓点推荐</router-link>
      </div>
    </div>
    <el-dialog v-model="reportDialogVisible" title="举报讨论" width="min(92vw, 420px)" class="responsive-dialog">
      <el-input v-model="reportReason" type="textarea" :rows="3" placeholder="请填写举报原因" />
      <template #footer>
        <el-button @click="reportDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitWikiReport">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import request from '../api/request'

const route = useRoute(), userStore = useUserStore()
const entry = ref(null), histories = ref([]), relatedEntries = ref([]), wikiComments = ref([])
const editing = ref(false), saving = ref(false), wikiUploading = ref(false), lastImgUrl = ref('')
const commentContent = ref(''), replyContent = ref(''), replyTarget = ref(null)
const reportDialogVisible = ref(false), reportTarget = ref(null), reportReason = ref('')
const editForm = ref({ title: '', category: '', content: '' })
const renderedContent = computed(() => entry.value?.content ? DOMPurify.sanitize(marked(entry.value.content)) : '')

const formatTime = (t) => { if (!t) return ''; const d = new Date(t), now = new Date(), diff = (now - d) / 1000; if (diff < 60) return '刚刚'; if (diff < 3600) return Math.floor(diff / 60) + '分钟前'; if (diff < 86400) return Math.floor(diff / 3600) + '小时前'; return d.toLocaleDateString('zh-CN') }

const startEdit = () => {
  editForm.value = { title: entry.value.title, category: entry.value.category, content: entry.value.content }
  editing.value = true
}

const uploadWikiImage = async (e) => {
  const file = e.target.files[0]; if (!file) return
  wikiUploading.value = true
  const fd = new FormData(); fd.append('file', file)
  try {
    const r = await request.post('/api/upload/image', fd)
    if (r.code === 200) {
      editForm.value.content += `\n![图片](${r.data})\n`
      lastImgUrl.value = r.data
      ElMessage.success('图片已插入')
    }
  } catch (err) { ElMessage.error('上传失败') }
  wikiUploading.value = false; e.target.value = ''
}

const submitEdit = async () => {
  if (!editForm.value.content.trim()) return ElMessage.warning('内容不能为空')
  saving.value = true
  const r = await request.put(`/api/wiki/${entry.value.id}`, editForm.value)
  if (r.code === 200) { ElMessage.success('词条已更新'); editing.value = false; loadEntry() }
  saving.value = false
}

const loadWikiComments = async () => {
  const r = await request.get(`/api/wiki/${route.params.id}/comments`)
  if (r.code === 200) wikiComments.value = r.data || []
}

const submitWikiComment = async (parentId = null) => {
  const content = parentId ? replyContent.value : commentContent.value
  if (!content.trim()) return ElMessage.warning('评论内容不能为空')
  const r = await request.post(`/api/wiki/${route.params.id}/comments`, { content, parentId })
  if (r.code === 200) {
    ElMessage.success(parentId ? '回复成功' : '评论成功')
    commentContent.value = ''
    replyContent.value = ''
    replyTarget.value = null
    loadWikiComments()
  }
}

const likeWikiComment = async (comment) => {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录')
  const r = await request.post('/api/likes', { targetId: comment.id, targetType: 'WIKI_COMMENT' })
  if (r.code === 200) {
    const liked = r.data === '点赞成功'
    comment.likeCount = Math.max((comment.likeCount || 0) + (liked ? 1 : -1), 0)
  }
}

const reportWikiComment = (comment) => {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录')
  reportTarget.value = comment
  reportReason.value = ''
  reportDialogVisible.value = true
}

const submitWikiReport = async () => {
  if (!reportReason.value.trim()) return ElMessage.warning('请填写举报原因')
  const r = await request.post('/api/reports', { targetId: reportTarget.value.id, targetType: 'WIKI_COMMENT', reason: reportReason.value })
  if (r.code === 200) {
    ElMessage.success('已提交举报')
    reportDialogVisible.value = false
    reportTarget.value = null
    reportReason.value = ''
  }
}

const canDelete = (comment) => userStore.isLoggedIn && (userStore.isAdmin || comment.userId === userStore.userId)

const deleteWikiComment = async (comment) => {
  const r = await request.delete(`/api/wiki/comments/${comment.id}`)
  if (r.code === 200) {
    ElMessage.success('已删除')
    loadWikiComments()
  }
}

const loadEntry = async () => {
  const r = await request.get(`/api/wiki/${route.params.id}`); if (r.code === 200) entry.value = r.data
  const h = await request.get(`/api/wiki/${route.params.id}/history`); if (h.code === 200) histories.value = h.data || []
  loadWikiComments()
  // Load related entries from same category
  if (entry.value?.category) {
    const re = await request.get('/api/wiki', { params: { category: entry.value.category, page: 1, size: 10 } })
    if (re.code === 200) relatedEntries.value = (re.data.records || []).filter(e => e.id !== entry.value.id).slice(0, 8)
  }
}

watch(() => route.params.id, loadEntry)
onMounted(loadEntry)
</script>

<style scoped>
.detail-grid { grid-template-columns: minmax(0, 4fr) 1fr; }
.wiki-detail-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin: 8px 0; }
.wiki-detail-head h1 { font-size: 22px; color: var(--ink); line-height: 1.3; }
.wiki-detail-meta { display: flex; flex-wrap: wrap; gap: 10px 12px; margin-bottom: 16px; color: #8b98a8; font-size: 12px; }
.wiki-content { line-height: 1.8; font-size: 15px; color: #444; }
.wiki-content :deep(h1), .wiki-content :deep(h2), .wiki-content :deep(h3) { margin: 16px 0 8px; color: #222; }
.wiki-content :deep(ul), .wiki-content :deep(ol) { padding-left: 20px; }
.wiki-content :deep(strong) { color: var(--color-primary-dark); }
.wiki-content :deep(code) { background: #f5f5f5; padding: 1px 4px; border-radius: 3px; font-size: 13px; }
.wiki-content :deep(pre) { background: #f5f5f5; padding: 12px; border-radius: 6px; overflow-x: auto; }
.wiki-content :deep(img) { max-width: 100%; border-radius: 6px; margin: 8px 0; }
.section-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.section-head h3 { font-size: 15px; margin: 0; }
.comment-editor { display: flex; flex-direction: column; gap: 8px; margin-bottom: 12px; }
.comment-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }
.login-tip { padding: 10px; background: #f6fbff; border: 1px solid #d8eafa; border-radius: 6px; margin-bottom: 10px; font-size: 13px; }
.comment-item { padding: 10px 0; border-bottom: 1px solid #f5f5f5; }
.comment-main { display: flex; gap: 10px; align-items: flex-start; }
.comment-avatar { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; flex: 0 0 auto; background: #f5f5f5; }
.comment-avatar.small { width: 28px; height: 28px; }
.comment-body { flex: 1; min-width: 0; }
.comment-body p { margin: 6px 0; line-height: 1.6; white-space: pre-wrap; word-break: break-word; }
.comment-meta { display: flex; gap: 8px; align-items: center; font-size: 12px; color: #8a98a8; }
.comment-meta b { color: #334155; font-size: 13px; }
.comment-tools { display: flex; gap: 4px; flex-wrap: wrap; }
.reply-editor { margin-top: 8px; }
.child-comment { margin-top: 8px; margin-left: 0; padding: 8px 0 0 0; border-top: 1px solid #eef3f8; border-bottom: 0; }
.side-stat { display: flex; justify-content: space-between; padding: 5px 0; font-size: 13px; border-bottom: 1px solid #f5f5f5; }
.side-stat:last-child { border-bottom: none; }
.side-item { display: flex; align-items: center; gap: 8px; padding: 5px 0; font-size: 13px; }
.quick-link { display: block; padding: 5px 0; font-size: 13px; color: #555; }
.quick-link:hover { color: var(--color-primary-dark); }
.img-upload-btn { width: 100px; height: 36px; border: 1px dashed #ccc; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 12px; color: #999; cursor: pointer; }
.img-upload-btn:hover { border-color: var(--color-primary-dark); color: var(--color-primary-dark); }
@media (max-width: 900px) { .detail-grid { grid-template-columns: 1fr; } .side-col { display: none; } .wiki-detail-head { align-items: flex-start; flex-direction: column; } .comment-actions { flex-wrap: wrap; } }
</style>
