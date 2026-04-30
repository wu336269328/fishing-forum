<template>
  <div class="page-shell admin-page">
    <h1 class="page-title">用户管理</h1>
    <div class="admin-nav"><router-link to="/admin">仪表盘</router-link><router-link to="/admin/users">用户</router-link><router-link to="/admin/audit">审核</router-link><router-link to="/admin/announcements">公告</router-link></div>
    <div class="page-toolbar admin-toolbar"><el-input v-model="keyword" placeholder="搜索用户" size="small" clearable @keyup.enter="load" /><el-button type="primary" size="small" @click="load">搜索</el-button></div>
    <div class="admin-table-wrap">
      <el-table :data="users" border size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="role" label="角色" width="80"><template #default="{row}"><el-tag :type="row.role==='ADMIN'?'danger':'info'" size="small">{{ row.role }}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="150"><template #default="{row}">
          <el-tag v-if="row.isBanned" type="danger" size="small">封禁</el-tag>
          <el-tag v-if="row.mutedUntil && new Date(row.mutedUntil) > new Date()" type="warning" size="small">禁言</el-tag>
          <el-tag v-if="!row.isBanned && !(row.mutedUntil && new Date(row.mutedUntil) > new Date())" type="success" size="small">正常</el-tag>
        </template></el-table-column>
        <el-table-column label="操作" width="300" fixed="right"><template #default="{row}">
          <el-button text size="small" type="primary" :loading="actionLoading===`role-${row.id}`" @click="changeRole(row)">{{ row.role==='ADMIN'?'降级':'升级' }}</el-button>
          <el-button text size="small" :type="row.isBanned?'success':'danger'" :loading="actionLoading===`ban-${row.id}`" @click="toggleBan(row)">{{ row.isBanned?'解封':'封禁' }}</el-button>
          <el-button text size="small" :type="row.mutedUntil && new Date(row.mutedUntil) > new Date()?'success':'warning'" :loading="actionLoading===`mute-${row.id}`" @click="toggleMute(row)">{{ row.mutedUntil && new Date(row.mutedUntil) > new Date()?'解禁':'禁言' }}</el-button>
          <el-button text size="small" type="danger" :loading="actionLoading===`delete-${row.id}`" @click="del(row)">删除</el-button>
        </template></el-table-column>
      </el-table>
    </div>
    <div class="pagination-wrap" v-if="total>10"><el-pagination background layout="prev,pager,next" :total="total" :page-size="10" v-model:current-page="page" @current-change="load" /></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'
const users=ref([]),keyword=ref(''),page=ref(1),total=ref(0),actionLoading=ref('')
const load=async()=>{const r=await request.get('/api/admin/users',{params:{page:page.value,size:10,keyword:keyword.value}});if(r.code===200){users.value=r.data.records||[];total.value=r.data.total||0}}
const withAction=async(key,fn)=>{actionLoading.value=key;try{await fn()}finally{actionLoading.value=''}}
const changeRole=async(u)=>{await ElMessageBox.confirm(`确定要将 ${u.username} ${u.role==='ADMIN'?'降级为普通用户':'升级为管理员'}吗？`,'角色变更确认',{type:'warning'});await withAction(`role-${u.id}`,async()=>{const r=await request.put(`/api/admin/users/${u.id}/role`,{role:u.role==='ADMIN'?'USER':'ADMIN'});if(r.code===200){ElMessage.success('已修改');load()}})}
const toggleBan=async(u)=>{await ElMessageBox.confirm(`确定要${u.isBanned?'解封':'封禁'}用户 ${u.username} 吗？`,'封禁确认',{type:'warning'});await withAction(`ban-${u.id}`,async()=>{const r=await request.put(`/api/admin/users/${u.id}/ban`,{banned:!u.isBanned});if(r.code===200){ElMessage.success(u.isBanned?'已解封':'已封禁');load()}})}
const toggleMute=async(u)=>{const muted=!(u.mutedUntil && new Date(u.mutedUntil)>new Date());await ElMessageBox.confirm(`确定要${muted?'禁言':'解除禁言'}用户 ${u.username} 吗？`,'禁言确认',{type:'warning'});await withAction(`mute-${u.id}`,async()=>{const r=await request.put(`/api/admin/users/${u.id}/mute`,{muted,minutes:1440});if(r.code===200){ElMessage.success(muted?'已禁言':'已解除禁言');load()}})}
const del=async(u)=>{await ElMessageBox.confirm(`确定要删除用户 ${u.username} 吗？此操作不可撤销。`,'删除用户确认',{type:'error'});await withAction(`delete-${u.id}`,async()=>{const r=await request.delete(`/api/admin/users/${u.id}`);if(r.code===200){ElMessage.success('已删除');load()}})}
onMounted(load)
</script>

<style scoped>
.admin-toolbar .el-input { width: 240px; }
@media (max-width: 768px) { .admin-toolbar .el-input { width: 100%; } }
</style>
