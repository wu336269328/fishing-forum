<template>
  <div>
    <h1 class="page-title">编辑帖子</h1>
    <div v-if="loaded" class="card">
      <el-form label-position="top">
        <el-form-item label="板块">
          <el-select v-model="form.sectionId" placeholder="选择板块" style="width:100%">
            <el-option v-for="s in sections" :key="s.id" :label="`${s.icon} ${s.name}`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="标题（5-200字）" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="帖子内容..." />
        </el-form-item>
        <el-form-item label="插入图片">
          <div style="display:flex; gap:8px; flex-wrap:wrap; align-items:flex-start">
            <div v-for="(img, i) in images" :key="i" class="img-preview">
              <img :src="img" />
              <span class="img-del" @click="images.splice(i,1)">✕</span>
            </div>
            <label class="img-upload-btn" :class="{disabled:uploading}">
              <input type="file" accept="image/*" style="display:none" @change="uploadImage" :disabled="uploading" />
              {{ uploading ? '上传中...' : '+ 添加图片' }}
            </label>
          </div>
        </el-form-item>
        <div style="display:flex; gap:8px; justify-content:flex-end">
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="loading">保存修改</el-button>
        </div>
      </el-form>
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
const sections = ref([]), images = ref([])
const form = ref({ title: '', content: '', sectionId: null })

onMounted(async () => {
  const [sRes, pRes] = await Promise.all([
    request.get('/api/sections'),
    request.get(`/api/posts/${route.params.id}`)
  ])
  if (sRes.code === 200) sections.value = sRes.data || []
  if (pRes.code === 200) {
    const p = pRes.data
    form.value = { title: p.title, content: p.content, sectionId: p.sectionId }
    // 提取已有图片
    const imgRegex = /<img[^>]+src="([^"]+)"/g
    let m; while ((m = imgRegex.exec(p.content)) !== null) images.value.push(m[1])
    // 去掉HTML标签，保留纯文本编辑
    form.value.content = p.content.replace(/<img[^>]*>/g, '').replace(/<\/?p>/g, '\n').replace(/<[^>]+>/g, '').trim()
  } else {
    ElMessage.error('帖子不存在')
    router.push('/forum')
  }
  loaded.value = true
})

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

const handleSubmit = async () => {
  if (!form.value.title || form.value.title.length < 5) return ElMessage.warning('标题至少5字')
  if (!form.value.content && !images.value.length) return ElMessage.warning('请输入内容')
  loading.value = true
  let content = form.value.content || ''
  if (!content.includes('<')) content = content.split('\n').filter(Boolean).map(p => `<p>${p}</p>`).join('')
  if (images.value.length) content += images.value.map(url => `<p><img src="${url}" style="max-width:100%"/></p>`).join('')
  const r = await request.put(`/api/posts/${route.params.id}`, { ...form.value, content })
  if (r.code === 200) { ElMessage.success('修改成功'); router.push(`/post/${route.params.id}`) }
  else ElMessage.error(r.message)
  loading.value = false
}
</script>

<style scoped>
.img-preview { position: relative; width: 80px; height: 80px; border-radius: 6px; overflow: hidden; border: 1px solid #eee; }
.img-preview img { width: 100%; height: 100%; object-fit: cover; }
.img-del { position: absolute; top: 2px; right: 2px; background: rgba(0,0,0,0.5); color: #fff; width: 18px; height: 18px; border-radius: 50%; font-size: 11px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.img-upload-btn { width: 80px; height: 80px; border: 1px dashed #ccc; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 12px; color: #999; cursor: pointer; text-align: center; padding: 4px; }
.img-upload-btn:hover { border-color: #1a73e8; color: #1a73e8; }
.img-upload-btn.disabled { cursor: not-allowed; opacity: 0.5; }
</style>
