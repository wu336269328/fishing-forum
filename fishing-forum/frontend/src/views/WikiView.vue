<template>
  <div>
    <h1 class="page-title">钓鱼百科</h1>
    <div class="card" style="display:flex; gap:8px; flex-wrap:wrap; align-items:center">
      <el-tag v-for="c in ['','鱼种','饵料','装备','技巧','常识']" :key="c" :effect="selectedCat===c?'dark':'plain'" @click="selectedCat=c" style="cursor:pointer">{{ c||'全部' }}</el-tag>
      <el-input v-model="keyword" placeholder="搜索..." clearable size="small" style="width:160px; margin-left:auto" @keyup.enter="load" />
      <el-button type="primary" size="small" @click="showCreate=true">+ 创建词条</el-button>
    </div>
    <div v-for="e in entries" :key="e.id" class="card" style="cursor:pointer" @click="$router.push(`/wiki/${e.id}`)">
      <div style="display:flex; justify-content:space-between"><b>{{ e.title }}</b><el-tag size="small" effect="plain">{{ e.category }}</el-tag></div>
      <p style="font-size:13px; color:#777; margin:4px 0">{{ e.content?.replace(/[#*\n]/g,' ').substring(0,80) }}...</p>
      <div style="font-size:12px; color:#999">✏️ {{ e.authorName }} · v{{ e.version }} · 👁 {{ e.viewCount }}</div>
    </div>
    <el-empty v-if="!entries.length" description="暂无词条" :image-size="40" />
    <div class="pagination-wrap" v-if="total>12"><el-pagination background layout="prev,pager,next" :total="total" :page-size="12" v-model:current-page="page" @current-change="load" /></div>

    <el-dialog v-model="showCreate" title="创建词条" width="500px">
      <el-form :model="form" label-position="top" size="small">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.category" style="width:100%"><el-option v-for="c in ['鱼种','饵料','装备','技巧','常识']" :key="c" :label="c" :value="c" /></el-select></el-form-item>
        <el-form-item label="内容（Markdown）"><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showCreate=false">取消</el-button><el-button type="primary" @click="submit">创建</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const entries = ref([]), selectedCat = ref(''), keyword = ref(''), page = ref(1), total = ref(0), showCreate = ref(false)
const form = ref({ title:'', category:'鱼种', content:'' })

const load = async () => { const r = await request.get('/api/wiki',{params:{category:selectedCat.value,keyword:keyword.value,page:page.value,size:12}}); if(r.code===200){ entries.value=r.data.records||[]; total.value=r.data.total||0 } }
const submit = async () => { if(!form.value.title||!form.value.content) return ElMessage.warning('请填写完整'); await request.post('/api/wiki',form.value); ElMessage.success('已创建'); showCreate.value=false; form.value={title:'',category:'鱼种',content:''}; load() }
watch(selectedCat, () => { page.value=1; load() })
onMounted(load)
</script>
