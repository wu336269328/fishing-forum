<template>
  <div>
    <h1 class="page-title">用户管理</h1>
    <div class="admin-nav"><router-link to="/admin">仪表盘</router-link><router-link to="/admin/users">用户</router-link><router-link to="/admin/audit">审核</router-link><router-link to="/admin/announcements">公告</router-link></div>
    <div class="card" style="padding:8px; margin-bottom:12px"><el-input v-model="keyword" placeholder="搜索用户..." size="small" clearable style="width:200px" @keyup.enter="load" /></div>
    <el-table :data="users" border size="small">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="80"><template #default="{row}"><el-tag :type="row.role==='ADMIN'?'danger':'info'" size="small">{{ row.role }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180"><template #default="{row}">
        <el-button text size="small" type="primary" @click="changeRole(row)">{{ row.role==='ADMIN'?'降级':'升级' }}</el-button>
        <el-popconfirm title="确定？" @confirm="del(row.id)"><template #reference><el-button text size="small" type="danger">删除</el-button></template></el-popconfirm>
      </template></el-table-column>
    </el-table>
    <div class="pagination-wrap" v-if="total>10"><el-pagination background layout="prev,pager,next" :total="total" :page-size="10" v-model:current-page="page" @current-change="load" /></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'
const users=ref([]),keyword=ref(''),page=ref(1),total=ref(0)
const load=async()=>{const r=await request.get('/api/admin/users',{params:{page:page.value,size:10,keyword:keyword.value}});if(r.code===200){users.value=r.data.records||[];total.value=r.data.total||0}}
const changeRole=async(u)=>{await request.put(`/api/admin/users/${u.id}/role`,{role:u.role==='ADMIN'?'USER':'ADMIN'});ElMessage.success('已修改');load()}
const del=async(id)=>{await request.delete(`/api/admin/users/${id}`);ElMessage.success('已删除');load()}
onMounted(load)
</script>

<style scoped>.admin-nav{display:flex;gap:12px;margin-bottom:16px;padding:8px 0;border-bottom:1px solid #eee;font-size:14px}.admin-nav a{color:#555;text-decoration:none}.admin-nav a:hover,.admin-nav a.router-link-active{color:#1a73e8}</style>
