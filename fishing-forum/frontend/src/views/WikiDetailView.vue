<template>
  <div v-if="entry" class="detail-grid">
    <!-- 主内容区 -->
    <div class="main-col">
      <div class="card">
        <el-button text size="small" @click="$router.push('/wiki')">← 返回百科</el-button>
        <div style="display:flex; justify-content:space-between; align-items:center; margin:8px 0">
          <h1 style="font-size:20px">{{ entry.title }}</h1>
          <el-button v-if="userStore.isLoggedIn" size="small" @click="startEdit">✏️ 编辑词条</el-button>
        </div>
        <div style="font-size:12px; color:#999; margin-bottom:16px; display:flex; gap:12px">
          <span>{{ entry.category }}</span>
          <span>作者: <span class="text-link" @click="$router.push(`/profile/${entry.userId}`)">{{ entry.authorName }}</span></span>
          <span>版本 v{{ entry.version }}</span>
          <span>👁 {{ entry.viewCount }}</span>
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
        <h3 style="font-size:15px; margin-bottom:8px">💬 词条讨论</h3>
        <p class="text-muted">百科讨论功能需要独立的数据模型支持，当前页面不再复用帖子评论接口，避免词条 ID 与帖子 ID 冲突导致数据串联。</p>
      </div>
    </div>

    <!-- 侧边栏 -->
    <div class="side-col">
      <!-- 词条信息 -->
      <div class="card">
        <h3 style="font-size:14px; margin-bottom:10px">📋 词条信息</h3>
        <div class="side-stat"><span>📂 分类</span><b>{{ entry.category }}</b></div>
        <div class="side-stat"><span>📝 作者</span><b>{{ entry.authorName }}</b></div>
        <div class="side-stat"><span>📊 版本</span><b>v{{ entry.version }}</b></div>
        <div class="side-stat"><span>👁 浏览</span><b>{{ entry.viewCount }}</b></div>
      </div>

      <!-- 编辑历史 -->
      <div class="card">
        <h3 style="font-size:14px; margin-bottom:10px">📜 编辑历史</h3>
        <div v-for="h in histories" :key="h.id" style="display:flex; gap:8px; padding:5px 0; border-bottom:1px solid #f5f5f5; font-size:12px; align-items:center">
          <el-tag size="small" effect="plain">v{{ h.version }}</el-tag>
          <span style="flex:1">{{ h.authorName }}</span>
          <span class="text-muted">{{ formatTime(h.createdAt) }}</span>
        </div>
        <el-empty v-if="!histories.length" description="暂无" :image-size="24" />
      </div>

      <!-- 同类词条 -->
      <div class="card" v-if="relatedEntries.length">
        <h3 style="font-size:14px; margin-bottom:10px">📖 相关词条</h3>
        <div v-for="re in relatedEntries" :key="re.id" class="side-item" style="cursor:pointer" @click="$router.push(`/wiki/${re.id}`)">
          <span style="font-size:13px; flex:1; overflow:hidden; text-overflow:ellipsis; white-space:nowrap">{{ re.title }}</span>
          <span class="text-muted">{{ re.category }}</span>
        </div>
      </div>

      <!-- 百科分类 -->
      <div class="card">
        <h3 style="font-size:14px; margin-bottom:10px">🗂️ 百科分类</h3>
        <router-link v-for="cat in ['鱼种','饵料','装备','技巧','常识','鱼种图鉴']" :key="cat" :to="`/wiki?category=${cat}`" class="quick-link">📁 {{ cat }}</router-link>
      </div>

      <div class="card">
        <h3 style="font-size:14px; margin-bottom:10px">🔗 快捷入口</h3>
        <router-link to="/wiki" class="quick-link">📖 百科首页</router-link>
        <router-link to="/forum" class="quick-link">📋 论坛首页</router-link>
        <router-link to="/spots" class="quick-link">📍 钓点推荐</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import request from '../api/request'

const route = useRoute(), userStore = useUserStore()
const entry = ref(null), histories = ref([]), relatedEntries = ref([])
const editing = ref(false), saving = ref(false), wikiUploading = ref(false), lastImgUrl = ref('')
const editForm = ref({ title: '', category: '', content: '' })
const renderedContent = computed(() => entry.value?.content ? marked(entry.value.content) : '')

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

const loadEntry = async () => {
  const r = await request.get(`/api/wiki/${route.params.id}`); if (r.code === 200) entry.value = r.data
  const h = await request.get(`/api/wiki/${route.params.id}/history`); if (h.code === 200) histories.value = h.data || []
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
.detail-grid { display: grid; grid-template-columns: 4fr 1fr; gap: 16px; }
.wiki-content { line-height: 1.8; font-size: 15px; color: #444; }
.wiki-content :deep(h1), .wiki-content :deep(h2), .wiki-content :deep(h3) { margin: 16px 0 8px; color: #222; }
.wiki-content :deep(ul), .wiki-content :deep(ol) { padding-left: 20px; }
.wiki-content :deep(strong) { color: #1a73e8; }
.wiki-content :deep(code) { background: #f5f5f5; padding: 1px 4px; border-radius: 3px; font-size: 13px; }
.wiki-content :deep(pre) { background: #f5f5f5; padding: 12px; border-radius: 6px; overflow-x: auto; }
.wiki-content :deep(img) { max-width: 100%; border-radius: 6px; margin: 8px 0; }
.comment-item { padding: 10px 0; border-bottom: 1px solid #f5f5f5; }
.side-stat { display: flex; justify-content: space-between; padding: 5px 0; font-size: 13px; border-bottom: 1px solid #f5f5f5; }
.side-stat:last-child { border-bottom: none; }
.side-item { display: flex; align-items: center; gap: 8px; padding: 5px 0; font-size: 13px; }
.quick-link { display: block; padding: 5px 0; font-size: 13px; color: #555; }
.quick-link:hover { color: #1a73e8; }
.img-upload-btn { width: 100px; height: 36px; border: 1px dashed #ccc; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 12px; color: #999; cursor: pointer; }
.img-upload-btn:hover { border-color: #1a73e8; color: #1a73e8; }
@media (max-width: 900px) { .detail-grid { grid-template-columns: 1fr; } .side-col { display: none; } }
</style>
