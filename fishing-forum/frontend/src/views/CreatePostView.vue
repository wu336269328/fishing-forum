<template>
  <div class="page-shell create-page">
    <section class="hero-panel">
      <p class="eyebrow">Create Post</p>
      <h1 class="hero-title">写下这次出钓的关键经验。</h1>
      <p class="hero-subtitle">标题说清楚问题，图片补足细节，标签让更多钓友找到它。</p>
    </section>
    <div class="create-workbench">
    <div class="card create-editor-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <!-- 帖子类型 -->
        <el-form-item label="帖子类型" prop="postType">
          <el-radio-group v-model="form.postType" size="small">
            <el-radio-button value="NORMAL">普通讨论</el-radio-button>
            <el-radio-button value="CATCH">渔获日记</el-radio-button>
            <el-radio-button value="REVIEW">装备测评</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="板块" prop="sectionId">
          <el-select v-model="form.sectionId" placeholder="选择板块" style="width:100%">
            <el-option v-for="s in sections" :key="s.id" :label="`${s.icon} ${s.name}`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tagId" placeholder="选择标签（可选）" clearable style="width:100%">
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="标题（5-200字）" maxlength="200" show-word-limit />
        </el-form-item>

        <!-- 渔获日记表单 -->
        <div v-if="form.postType==='CATCH'" class="sub-form">
          <h4 class="sub-form-title success">渔获信息</h4>
          <div class="sub-grid">
            <el-form-item label="鱼种"><el-input v-model="catchForm.fishSpecies" placeholder="如：鲫鱼" /></el-form-item>
            <el-form-item label="重量(斤)"><el-input-number v-model="catchForm.weight" :min="0" :precision="1" style="width:100%" /></el-form-item>
            <el-form-item label="饵料"><el-input v-model="catchForm.bait" placeholder="如：蚯蚓、商品饵" /></el-form-item>
            <el-form-item label="钓点"><el-input v-model="catchForm.spotName" placeholder="如：翠湖水库" /></el-form-item>
            <el-form-item label="天气"><el-input v-model="catchForm.weather" placeholder="如：晴 微风" /></el-form-item>
            <el-form-item label="垂钓日期"><el-date-picker v-model="catchForm.fishingDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
          </div>
        </div>

        <!-- 装备测评表单 -->
        <div v-if="form.postType==='REVIEW'" class="sub-form">
          <h4 class="sub-form-title warning">装备信息</h4>
          <div class="sub-grid">
            <el-form-item label="品牌"><el-input v-model="reviewForm.brand" placeholder="如：光威、化氏" /></el-form-item>
            <el-form-item label="型号"><el-input v-model="reviewForm.model" placeholder="如：天流 V4.5米" /></el-form-item>
            <el-form-item label="分类">
              <el-select v-model="reviewForm.gearCategory" style="width:100%">
                <el-option label="鱼竿" value="鱼竿" /><el-option label="鱼线" value="鱼线" />
                <el-option label="鱼钩" value="鱼钩" /><el-option label="浮漂" value="浮漂" />
                <el-option label="饵料" value="饵料" /><el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="价格(元)"><el-input-number v-model="reviewForm.price" :min="0" style="width:100%" /></el-form-item>
            <el-form-item label="评分">
              <el-rate v-model="reviewForm.rating" />
            </el-form-item>
          </div>
          <el-form-item label="优点"><el-input v-model="reviewForm.pros" type="textarea" :rows="2" placeholder="这款装备的优点..." /></el-form-item>
          <el-form-item label="缺点"><el-input v-model="reviewForm.cons" type="textarea" :rows="2" placeholder="这款装备的不足..." /></el-form-item>
        </div>

        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="正文内容（5-50000 字符），上传图片会自动追加到末尾" />
        </el-form-item>
        <el-form-item label="插入图片">
          <div class="image-upload-row">
            <div v-for="(img, i) in images" :key="i" class="img-preview">
              <img :src="img" />
              <span class="img-del" @click="images.splice(i,1)">✕</span>
            </div>
            <label class="img-upload-btn" :class="{disabled:uploading}">
              <input type="file" accept="image/*" style="display:none" @change="uploadImage" :disabled="uploading" />
              {{ uploading ? '上传中...' : '+ 添加图片' }}
            </label>
          </div>
          <div v-if="images.length" class="text-muted upload-count">已上传 {{ images.length }} 张图片</div>
        </el-form-item>
        <div class="form-actions">
          <el-button @click="$router.back()">取消</el-button>
          <el-button @click="saveDraft">保存草稿</el-button>
          <el-button v-if="hasDraft" @click="clearDraft">清空草稿</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="loading">发布</el-button>
        </div>
      </el-form>
    </div>
    <aside class="card publish-aside">
      <h3>发布检查</h3>
      <div class="publish-check" :class="{ done: form.sectionId }">选择板块</div>
      <div class="publish-check" :class="{ done: form.title.length >= 5 }">标题至少 5 字</div>
      <div class="publish-check" :class="{ done: form.content || images.length }">正文或图片</div>
      <p class="text-muted">完成关键项后即可发布，图片会插入到正文末尾。</p>
    </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { normalizePostType } from '../utils/postType'

const route = useRoute(), router = useRouter(), loading = ref(false), uploading = ref(false)
const sections = ref([]), tags = ref([]), images = ref([])
const hasDraft = ref(false)
const formRef = ref(null)
const DRAFT_KEY = 'fishforum:create-post-draft'
const form = ref({ title: '', content: '', sectionId: null, tagId: null, postType: 'NORMAL' })

const validateContent = (rule, value, callback) => {
  if (!value && !images.value.length) return callback(new Error('请填写正文或至少上传一张图片'))
  if (value && value.length > 50000) return callback(new Error('正文长度不能超过 50000 字符'))
  callback()
}

const rules = {
  postType: [{ required: true, message: '请选择帖子类型', trigger: 'change' }],
  sectionId: [{ required: true, message: '请选择板块', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度 5-200 字', trigger: 'blur' }
  ],
  content: [{ validator: validateContent, trigger: 'blur' }]
}
const catchForm = ref({ fishSpecies: '', weight: null, bait: '', spotName: '', weather: '', fishingDate: '' })
const reviewForm = ref({ brand: '', model: '', gearCategory: '鱼竿', price: null, rating: 3, pros: '', cons: '' })

const draftPayload = () => ({
  form: form.value,
  catchForm: catchForm.value,
  reviewForm: reviewForm.value,
  images: images.value,
  savedAt: new Date().toISOString()
})

const saveDraft = () => {
  localStorage.setItem(DRAFT_KEY, JSON.stringify(draftPayload()))
  hasDraft.value = true
  ElMessage.success('草稿已保存')
}

const restoreDraft = () => {
  const raw = localStorage.getItem(DRAFT_KEY)
  hasDraft.value = !!raw
  if (!raw) return
  try {
    const draft = JSON.parse(raw)
    form.value = { ...form.value, ...(draft.form || {}) }
    catchForm.value = { ...catchForm.value, ...(draft.catchForm || {}) }
    reviewForm.value = { ...reviewForm.value, ...(draft.reviewForm || {}) }
    images.value = draft.images || []
    ElMessage.info('已恢复上次草稿')
  } catch (e) {
    localStorage.removeItem(DRAFT_KEY)
    hasDraft.value = false
  }
}

const clearDraft = () => {
  localStorage.removeItem(DRAFT_KEY)
  hasDraft.value = false
  ElMessage.success('草稿已清空')
}

const uploadImage = async (e) => {
  const file = e.target.files[0]; if (!file) return
  uploading.value = true
  const fd = new FormData(); fd.append('file', file)
  try {
    const r = await request.post('/api/upload/image', fd)
    if (r.code === 200) { images.value.push(r.data); ElMessage.success('图片已上传') }
    else ElMessage.error(r.message)
  } catch (err) { ElMessage.error('上传失败') }
  uploading.value = false
  e.target.value = ''
}

const loadTags = async () => {
  const params = form.value.sectionId ? { sectionId: form.value.sectionId } : {}
  const r = await request.get('/api/tags', { params })
  if (r.code === 200) tags.value = r.data || []
}
watch(() => form.value.sectionId, loadTags)

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    ElMessage.warning('请检查表单中的必填项')
    return
  }
  loading.value = true
  let content = form.value.content || ''
  if (!content.includes('<')) content = content.split('\n').filter(Boolean).map(p => `<p>${p}</p>`).join('')
  if (images.value.length) content += images.value.map(url => `<p><img src="${url}" style="max-width:100%"/></p>`).join('')

  const body = { ...form.value, content }
  if (form.value.postType === 'CATCH') body.catchRecord = catchForm.value
  if (form.value.postType === 'REVIEW') body.gearReview = reviewForm.value

  try {
    const r = await request.post('/api/posts', body)
    if (r.code === 200) {
      clearDraft()
      ElMessage.success('发帖成功')
      router.push(`/post/${r.data.id}`)
    } else {
      ElMessage.error(r.message || '发帖失败，请稍后重试')
    }
  } catch (e) {
    ElMessage.error('发帖失败，请稍后重试')
  }
  loading.value = false
}

watch([form, catchForm, reviewForm, images], () => {
  if (form.value.title || form.value.content || images.value.length) {
    localStorage.setItem(DRAFT_KEY, JSON.stringify(draftPayload()))
    hasDraft.value = true
  }
}, { deep: true })

onMounted(async () => {
  restoreDraft()
  form.value.postType = normalizePostType(route.query.postType)
  const r = await request.get('/api/sections'); if (r.code === 200) sections.value = r.data || []
  await loadTags()
})
</script>

<style scoped>
.create-page { max-width: 1240px; margin: 0 auto; }
.create-workbench { display: grid; grid-template-columns: minmax(0, 1fr) 280px; gap: 16px; align-items: start; }
.create-editor-card { min-width: 0; }
.publish-aside { position: sticky; top: 82px; }
.publish-aside h3 { font-size: 16px; margin-bottom: 10px; }
.publish-check { padding: 9px 0; border-bottom: 1px solid var(--line); color: var(--muted); font-size: 13px; }
.publish-check::before { content: "○"; margin-right: 6px; color: #94a3b8; }
.publish-check.done { color: var(--color-primary-dark); font-weight: 700; }
.publish-check.done::before { content: "✓"; color: var(--color-primary); }
.sub-form { background: var(--surface-muted); border-radius: 14px; padding: 14px 16px; margin-bottom: 12px; border: 1px solid var(--border-subtle); }
.sub-form-title { font-size: 14px; margin-bottom: 8px; }
.sub-form-title.success { color: var(--color-success); }
.sub-form-title.warning { color: var(--color-warning); }
.sub-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.form-actions { display: flex; gap: 8px; justify-content: flex-end; position: sticky; bottom: calc(var(--tabbar-offset) + 12px); z-index: 20; background: linear-gradient(180deg, rgba(255,255,255,0), rgba(255,255,255,.96) 24%); padding-top: 12px; }
.image-upload-row { display: flex; gap: 8px; flex-wrap: wrap; align-items: flex-start; }
.img-preview { position: relative; width: 80px; height: 80px; border-radius: 6px; overflow: hidden; border: 1px solid #eee; }
.img-preview img { width: 100%; height: 100%; object-fit: cover; }
.img-del { position: absolute; top: 2px; right: 2px; background: rgba(0,0,0,0.5); color: #fff; width: 18px; height: 18px; border-radius: 50%; font-size: 11px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.img-upload-btn { width: 80px; height: 80px; border: 1px dashed #ccc; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 12px; color: #999; cursor: pointer; text-align: center; padding: 4px; }
.img-upload-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.upload-count { margin-top: 4px; }
.img-upload-btn.disabled { cursor: not-allowed; opacity: 0.5; }
@media (max-width: 768px) {
  .create-workbench { grid-template-columns: 1fr; }
  .publish-aside { display: block; position: static; order: -1; }
  .publish-aside p { display: none; }
  :deep(.el-radio-group) { display: grid; grid-template-columns: 1fr; gap: 8px; width: 100%; }
  :deep(.el-radio-button__inner) { width: 100%; border-left: var(--el-border) !important; border-radius: 12px !important; }
  .sub-grid { grid-template-columns: 1fr; }
  .form-actions { justify-content: stretch; }
  .form-actions .el-button { flex: 1; }
}
</style>
