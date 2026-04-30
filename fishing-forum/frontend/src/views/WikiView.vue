<template>
  <div class="page-shell">
    <section class="hero-panel">
      <p class="eyebrow">Wiki</p>
      <h1 class="hero-title">钓鱼百科</h1>
      <p class="hero-subtitle">鱼种、饵料、装备和技巧，钓友共建的可搜索知识库。</p>
    </section>
    <div class="page-toolbar wiki-toolbar">
      <div class="filter-chip-row">
        <button
          v-for="c in categories"
          :key="c.value"
          type="button"
          class="ui-chip"
          :class="{ active: selectedCat === c.value }"
          @click="selectedCat = c.value"
        >{{ c.label }}</button>
      </div>
      <el-input v-model="keyword" placeholder="搜索词条..." clearable size="small" class="wiki-search" @keyup.enter="load" />
      <el-button type="primary" size="small" @click="showCreate=true">+ 创建词条</el-button>
    </div>
    <article
      v-for="e in entries"
      :key="e.id"
      class="card wiki-item list-card"
      role="button"
      tabindex="0"
      @click="$router.push(`/wiki/${e.id}`)"
      @keydown.enter.space.prevent="$router.push(`/wiki/${e.id}`)"
    >
      <img v-if="getThumb(e.content)" :src="getThumb(e.content)" class="wiki-thumb" loading="lazy" />
      <div class="wiki-info">
        <div class="wiki-head">
          <b class="wiki-title">{{ e.title }}</b>
          <el-tag size="small" effect="plain">{{ e.category }}</el-tag>
        </div>
        <p class="wiki-excerpt">{{ e.content?.replace(/!\[.*?\]\(.*?\)/g,'').replace(/[#*\n\r]/g,' ').replace(/\s+/g,' ').substring(0,100) }}...</p>
        <div class="wiki-meta">{{ e.authorName }} · v{{ e.version }} · 浏览 {{ e.viewCount }}</div>
      </div>
    </article>
    <div v-if="loadError" class="card desktop-error-card">
      <div>
        <b>百科加载失败</b>
        <p class="text-muted">后端暂时不可用，刷新或稍后重试。</p>
      </div>
      <el-button size="small" type="primary" @click="load">重新加载</el-button>
    </div>
    <div v-else-if="!entries.length" class="card empty-state">
      <div class="empty-state-icon">📚</div>
      <div class="empty-state-title">{{ keyword ? '没找到相关词条' : '这个分类还没有词条' }}</div>
      <div class="empty-state-text">{{ keyword ? '换个关键词，或贡献一个新词条让大家受益。' : '把你熟悉的钓鱼知识写下来，帮助更多新人。' }}</div>
      <el-button type="primary" size="small" round @click="showCreate=true">创建词条</el-button>
    </div>
    <div class="pagination-wrap" v-if="total>12"><el-pagination background layout="prev,pager,next" :total="total" :page-size="12" v-model:current-page="page" @current-change="load" /></div>

    <el-dialog v-model="showCreate" title="创建词条" width="min(92vw, 500px)" class="responsive-dialog">
      <el-form :model="form" label-position="top" size="small">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.category" style="width:100%"><el-option v-for="c in ['鱼种','饵料','装备','技巧','常识','鱼种图鉴']" :key="c" :label="c" :value="c" /></el-select></el-form-item>
        <el-form-item label="内容（Markdown）"><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showCreate=false">取消</el-button><el-button type="primary" @click="submit">创建</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const route = useRoute(), router = useRouter()
const entries = ref([]), selectedCat = ref(''), keyword = ref(''), page = ref(1), total = ref(0), showCreate = ref(false)
const form = ref({ title:'', category:'鱼种', content:'' })
const loadError = ref(false)

const categories = [
  { value: '', label: '全部' },
  { value: '鱼种', label: '鱼种' },
  { value: '饵料', label: '饵料' },
  { value: '装备', label: '装备' },
  { value: '技巧', label: '技巧' },
  { value: '常识', label: '常识' },
  { value: '鱼种图鉴', label: '鱼种图鉴' }
]

// 从 markdown 内容中提取第一张图片 URL
const getThumb = (content) => {
  if (!content) return null
  const m = content.match(/!\[.*?\]\((.*?)\)/)
  return m ? m[1] : null
}

const load = async () => {
  loadError.value = false
  try {
    const r = await request.get('/api/wiki',{params:{category:selectedCat.value,keyword:keyword.value,page:page.value,size:12}})
    if(r.code===200){ entries.value=r.data.records||[]; total.value=r.data.total||0 }
  } catch (e) {
    loadError.value = true
    entries.value = []
  }
}
const submit = async () => { if(!form.value.title||!form.value.content) return ElMessage.warning('请填写完整'); await request.post('/api/wiki',form.value); ElMessage.success('已创建'); showCreate.value=false; form.value={title:'',category:'鱼种',content:''}; load() }

watch(selectedCat, (v) => {
  page.value = 1
  // 同步到 URL，便于刷新和返回保持状态
  const query = { ...route.query }
  if (v) query.category = v
  else delete query.category
  router.replace({ query })
  load()
})

watch(() => route.query.category, (v) => {
  const next = (typeof v === 'string') ? v : ''
  if (selectedCat.value !== next) selectedCat.value = next
})

onMounted(() => {
  if (typeof route.query.category === 'string') selectedCat.value = route.query.category
  load()
})
</script>

<style scoped>
.wiki-toolbar { gap: 10px; }
.wiki-search { width: 200px; margin-left: auto; }
.wiki-item { display: flex; gap: 12px; cursor: pointer; border-color: var(--border-subtle); }
.wiki-thumb { width: 100px; height: 100px; border-radius: 12px; object-fit: cover; flex-shrink: 0; background: var(--surface-muted); aspect-ratio: 1 / 1; }
.wiki-info { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; }
.wiki-head { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.wiki-title { font-size: 15px; font-weight: 700; color: var(--ink); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.wiki-excerpt { font-size: 13px; color: var(--muted); margin: 6px 0; line-height: 1.55; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.wiki-meta { font-size: 12px; color: var(--muted-soft); }
@media (max-width: 768px) {
  .wiki-toolbar { gap: 8px; }
  .wiki-search { width: 100%; margin-left: 0; }
}
</style>

