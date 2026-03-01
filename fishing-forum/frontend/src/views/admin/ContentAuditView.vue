<template>
  <div>
    <h1 class="page-title">内容审核</h1>
    <div class="admin-nav"><router-link to="/admin">仪表盘</router-link><router-link to="/admin/users">用户</router-link><router-link to="/admin/audit">审核</router-link><router-link to="/admin/announcements">公告</router-link></div>
    <div class="card" style="padding:8px; margin-bottom:12px"><el-select v-model="status" size="small" style="width:120px" clearable placeholder="状态"><el-option label="待处理" value="PENDING" /><el-option label="已处理" value="RESOLVED" /><el-option label="已驳回" value="REJECTED" /></el-select></div>
    <el-table :data="reports" border size="small">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="targetType" label="类型" width="70" />
      <el-table-column prop="targetId" label="目标" width="70" />
      <el-table-column prop="reason" label="原因" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag :type="{PENDING:'warning',RESOLVED:'success',REJECTED:'info'}[row.status]" size="small">{{ {PENDING:'待处理',RESOLVED:'已处理',REJECTED:'已驳回'}[row.status] }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="160"><template #default="{row}">
        <template v-if="row.status==='PENDING'">
          <el-button text size="small" type="success" @click="handle(row.id,'resolve')">通过</el-button>
          <el-button text size="small" @click="handle(row.id,'reject')">驳回</el-button>
        </template><span v-else style="font-size:12px;color:#ccc">已处理</span>
      </template></el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'
const reports=ref([]),status=ref('PENDING')
const load=async()=>{const r=await request.get('/api/admin/reports',{params:{page:1,size:50,status:status.value}});if(r.code===200) reports.value=r.data.records||[]}
const handle=async(id,action)=>{await request.put(`/api/admin/reports/${id}`,{action});ElMessage.success('已处理');load()}
watch(status,load); onMounted(load)
</script>

<style scoped>.admin-nav{display:flex;gap:12px;margin-bottom:16px;padding:8px 0;border-bottom:1px solid #eee;font-size:14px}.admin-nav a{color:#555;text-decoration:none}.admin-nav a:hover,.admin-nav a.router-link-active{color:#1a73e8}</style>
