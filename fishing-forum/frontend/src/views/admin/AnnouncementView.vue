<template>
  <div class="page-shell admin-page">
    <h1 class="page-title">公告管理</h1>
    <div class="admin-nav"><router-link to="/admin">仪表盘</router-link><router-link to="/admin/users">用户</router-link><router-link to="/admin/audit">审核</router-link><router-link to="/admin/announcements">公告</router-link></div>
    <div class="page-toolbar admin-toolbar"><el-button type="primary" size="small" @click="showCreate=true">发布公告</el-button></div>
    <div class="card sensitive-card">
      <h3>敏感词维护</h3>
      <div class="sensitive-form">
        <el-input v-model="word" size="small" placeholder="新增敏感词" />
        <el-button type="primary" size="small" @click="createWord">添加</el-button>
      </div>
      <div class="sensitive-tags">
        <el-tag v-for="w in words" :key="w.id" :type="w.isActive?'danger':'info'" @click="toggleWord(w)">
          {{ w.word }} · {{ w.isActive?'启用':'停用' }}
        </el-tag>
      </div>
    </div>
    <div v-for="a in announcements" :key="a.id" class="card announcement-card">
      <div class="announcement-head">
        <b>{{ a.title }}</b>
        <el-tag :type="a.isActive?'success':'info'" size="small">{{ a.isActive?'激活':'停用' }}</el-tag>
      </div>
      <p>{{ a.content }}</p>
      <div class="announcement-meta">
        <span>{{ new Date(a.createdAt).toLocaleDateString('zh-CN') }}</span>
        <span>
          <el-button text size="small" @click="toggle(a)">{{ a.isActive?'停用':'激活' }}</el-button>
          <el-button text size="small" type="danger" @click="del(a)">删除</el-button>
        </span>
      </div>
    </div>
    <el-empty v-if="!announcements.length" description="暂无" :image-size="40" />
    <el-dialog v-model="showCreate" title="发布公告" width="min(92vw, 460px)" class="responsive-dialog">
      <el-form :model="form" label-position="top" size="small">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showCreate=false">取消</el-button><el-button type="primary" :loading="saving" @click="create">发布</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'
const announcements=ref([]),showCreate=ref(false),form=ref({title:'',content:''}),words=ref([]),word=ref(''),saving=ref(false)
const load=async()=>{const r=await request.get('/api/admin/announcements');if(r.code===200) announcements.value=r.data||[];const w=await request.get('/api/admin/sensitive-words');if(w.code===200) words.value=w.data||[]}
const create=async()=>{if(!form.value.title||!form.value.content) return ElMessage.warning('请填写');saving.value=true;try{const r=await request.post('/api/admin/announcements',{...form.value,isActive:true});if(r.code===200){ElMessage.success('已发布');showCreate.value=false;form.value={title:'',content:''};load()}}finally{saving.value=false}}
const createWord=async()=>{if(!word.value.trim()) return ElMessage.warning('请填写敏感词');const r=await request.post('/api/admin/sensitive-words',{word:word.value});if(r.code===200){word.value='';ElMessage.success('已添加');load()}}
const toggleWord=async(w)=>{await ElMessageBox.confirm(`确定要${w.isActive?'停用':'启用'}敏感词「${w.word}」吗？`,'敏感词状态确认',{type:'warning'});const r=await request.put(`/api/admin/sensitive-words/${w.id}`,{active:!w.isActive});if(r.code===200) load()}
const toggle=async(a)=>{await ElMessageBox.confirm(`确定要${a.isActive?'停用':'激活'}公告「${a.title}」吗？`,'公告状态确认',{type:'warning'});const r=await request.put(`/api/admin/announcements/${a.id}`,{isActive:!a.isActive});if(r.code===200) load()}
const del=async(a)=>{await ElMessageBox.confirm(`确定要删除公告「${a.title}」吗？`,'删除公告确认',{type:'error'});const r=await request.delete(`/api/admin/announcements/${a.id}`);if(r.code===200){ElMessage.success('已删除');load()}}
onMounted(load)
</script>

<style scoped>
.sensitive-card h3 { font-size: 15px; margin-bottom: 10px; color: var(--ink); }
.sensitive-form { display: flex; gap: 8px; margin-bottom: 10px; }
.sensitive-form .el-input { max-width: 240px; }
.sensitive-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.sensitive-tags .el-tag { cursor: pointer; }
.announcement-card { border-color: #e3eaf2; }
.announcement-head { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.announcement-card p { font-size: 13px; color: #4b5563; margin: 6px 0; }
.announcement-meta { font-size: 12px; color: #9aa7b4; display: flex; justify-content: space-between; align-items: center; gap: 8px; }
@media (max-width: 768px) { .sensitive-form { flex-direction: column; } .sensitive-form .el-input { max-width: none; } .announcement-meta { align-items: flex-start; flex-direction: column; } }
</style>
