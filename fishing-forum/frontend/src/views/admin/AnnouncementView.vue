<template>
  <div>
    <h1 class="page-title">公告管理</h1>
    <div class="admin-nav"><router-link to="/admin">仪表盘</router-link><router-link to="/admin/users">用户</router-link><router-link to="/admin/audit">审核</router-link><router-link to="/admin/announcements">公告</router-link></div>
    <div class="card" style="padding:8px; margin-bottom:12px"><el-button type="primary" size="small" @click="showCreate=true">+ 发布公告</el-button></div>
    <div v-for="a in announcements" :key="a.id" class="card">
      <div style="display:flex; justify-content:space-between; align-items:center">
        <b>{{ a.title }}</b>
        <el-tag :type="a.isActive?'success':'info'" size="small">{{ a.isActive?'激活':'停用' }}</el-tag>
      </div>
      <p style="font-size:13px; color:#777; margin:4px 0">{{ a.content }}</p>
      <div style="font-size:12px; color:#ccc; display:flex; justify-content:space-between">
        <span>{{ new Date(a.createdAt).toLocaleDateString('zh-CN') }}</span>
        <span>
          <el-button text size="small" @click="toggle(a)">{{ a.isActive?'停用':'激活' }}</el-button>
          <el-popconfirm title="确定？" @confirm="del(a.id)"><template #reference><el-button text size="small" type="danger">删除</el-button></template></el-popconfirm>
        </span>
      </div>
    </div>
    <el-empty v-if="!announcements.length" description="暂无" :image-size="40" />
    <el-dialog v-model="showCreate" title="发布公告" width="420px">
      <el-form :model="form" label-position="top" size="small">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showCreate=false">取消</el-button><el-button type="primary" @click="create">发布</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'
const announcements=ref([]),showCreate=ref(false),form=ref({title:'',content:''})
const load=async()=>{const r=await request.get('/api/admin/announcements');if(r.code===200) announcements.value=r.data||[]}
const create=async()=>{if(!form.value.title||!form.value.content) return ElMessage.warning('请填写');await request.post('/api/admin/announcements',{...form.value,isActive:true});ElMessage.success('已发布');showCreate.value=false;form.value={title:'',content:''};load()}
const toggle=async(a)=>{await request.put(`/api/admin/announcements/${a.id}`,{isActive:!a.isActive});load()}
const del=async(id)=>{await request.delete(`/api/admin/announcements/${id}`);ElMessage.success('已删除');load()}
onMounted(load)
</script>

<style scoped>.admin-nav{display:flex;gap:12px;margin-bottom:16px;padding:8px 0;border-bottom:1px solid #eee;font-size:14px}.admin-nav a{color:#555;text-decoration:none}.admin-nav a:hover,.admin-nav a.router-link-active{color:#1a73e8}</style>
