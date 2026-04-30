<template>
  <div class="page-shell">
    <h1 class="page-title">编辑帖子</h1>
    <div v-if="loaded" class="card edit-editor-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="板块" prop="sectionId">
          <el-select v-model="form.sectionId" placeholder="选择板块" style="width:100%">
            <el-option v-for="s in sections" :key="s.id" :label="`${s.icon} ${s.name}`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="标题（5-200字）" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="正文内容..." />
        </el-form-item>
        <el-form-item label="插入图片">
          <div class="image-upload-row">
            <div v-for="(img, i) in images" :key="i" class="img-preview">
              <img :src="img" loading="lazy" />
              <span class="img-del" @click="images.splice(i,1)">✕</span>
            </div>
            <label class="img-upload-btn" :class="{disabled:uploading}">
              <input type="file" accept="image/*" style="display:none" @change="uploadImage" :disabled="uploading" />
              {{ uploading ? '上传中...' : '+ 添加图片' }}
            </label>
          </div>
        </el-form-item>
        <div class="edit-form-actions">
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="loading">保存修改</el-button>
        </div>
      </el-form>
    </div>
    <div v-else-if="loadError" class="card desktop-error-card">
      <div>
        <b>帖子加载失败</b>
        <p class="text-muted">无法获取帖子内容，请检查后端服务或稍后重试。</p>
      </div>
      <el-button size="small" type="primary" @click="loadData">重新加载</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const route = useRoute(), router = useRouter()
const loading = ref(false), uploading = ref(false), loaded = ref(false)
const loadError = ref(false)
const sections = ref([]), images = ref([])
const formRef = ref(null)
const form = ref({ title: '', content: '', sectionId: null })

const validateContent = (rule, value, callback) => {
  if (!value && !images.value.length) return callback(new Error('请填写正文或保留至少一张图片'))
  callback()
}

const rules = {
  sectionId: [{ required: true, message: '请选择板块', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度 5-200 字', trigger: 'blur' }
  ],
  content: [{ validator: validateContent, trigger: 'blur' }]
}

const loadData = async () => {
  loadError.value = false
  try {
    const [sRes, pRes] = await Promise.all([
      request.get('/api/sections'),
      request.get(`/api/posts/${route.params.id}`)
    ])
    if (sRes.code === 200) sections.value = sRes.data || []
    if (pRes.code === 200) {
      const p = pRes.data.post || pRes.data
      form.value = { title: p.title, content: p.content, sectionId: p.sectionId }
      // 提取已有图片
      const imgRegex = /<img[^>]+src="([^"]+)"/g
      let m; while ((m = imgRegex.exec(p.content)) !== null) images.value.push(m[1])
      // 去掉HTML标签，保留纯文本编辑
      form.value.content = p.content.replace(/<img[^>]*>/g, '').replace(/<\/?p>/g, '\n').replace(/<[^>]+>/g, '').trim()
    } else {
      ElMessage.error('帖子不存在')
      router.push('/forum')
      return
    }
    loaded.value = true
  } catch (e) {
    loadError.value = true
  }
}

onMounted(loadData)

const uploadImage = async (e) => {
  const file = e.target.files[0]; if (!file) return
  uploading.value = true
  const fd = new FormData(); fd.append('file', file)
  try {
    const r = await request.post('/api/upload/image', fd)
    if (r.code === 200) { images.value.push(r.data); ElMessage.success('图片已上传') }
    else ElMessage.error(r.message || '上传失败')
  } catch (err) { ElMessage.error('上传失败') }
  uploading.value = false
  e.target.value = ''
}

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
  try {
    const r = await request.put(`/api/posts/${route.params.id}`, { ...form.value, content })
    if (r.code === 200) { ElMessage.success('修改成功'); router.push(`/post/${route.params.id}`) }
    else ElMessage.error(r.message || '保存失败，请重试')
  } catch (e) {
    ElMessage.error('保存失败，请稍后重试')
  }
  loading.value = false
}
</script>

<style scoped>
.edit-editor-card { max-width: 860px; margin: 0 auto; }
.image-upload-row { display: flex; gap: 8px; flex-wrap: wrap; align-items: flex-start; }
.img-preview { position: relative; width: 80px; height: 80px; border-radius: 10px; overflow: hidden; border: 1px solid var(--border-subtle); }
.img-preview img { width: 100%; height: 100%; object-fit: cover; }
.img-del { position: absolute; top: 4px; right: 4px; background: rgba(0,0,0,0.55); color: #fff; width: 20px; height: 20px; border-radius: 50%; font-size: 12px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.img-upload-btn { width: 80px; height: 80px; border: 1px dashed var(--border-subtle); border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 12px; color: var(--muted); cursor: pointer; text-align: center; padding: 4px; }
.img-upload-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.edit-form-actions { display: flex; gap: 8px; justify-content: flex-end; position: sticky; bottom: calc(var(--tabbar-offset) + 12px); z-index: 20; background: linear-gradient(180deg, rgba(255,255,255,0), rgba(255,255,255,.96) 24%); padding-top: 12px; }
.img-upload-btn.disabled { cursor: not-allowed; opacity: 0.5; }
@media (max-width: 768px) {
  .edit-form-actions { justify-content: stretch; }
  .edit-form-actions .el-button { flex: 1; }
}
</style>
