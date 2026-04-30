<template>
  <div class="page-shell admin-page">
    <h1 class="page-title">内容审核</h1>
    <div class="admin-nav"><router-link to="/admin">仪表盘</router-link><router-link to="/admin/users">用户</router-link><router-link to="/admin/audit">审核</router-link><router-link to="/admin/announcements">公告</router-link></div>
    <div class="page-toolbar admin-toolbar"><el-select v-model="status" size="small" clearable placeholder="状态"><el-option label="待处理" value="PENDING" /><el-option label="已处理" value="RESOLVED" /><el-option label="已驳回" value="REJECTED" /></el-select></div>
    <div class="admin-table-wrap">
      <el-table :data="reports" border size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="targetType" label="类型" width="90" />
        <el-table-column prop="targetId" label="目标" width="80" />
        <el-table-column prop="reason" label="原因" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90"><template #default="{row}"><el-tag :type="{PENDING:'warning',RESOLVED:'success',REJECTED:'info'}[row.status]" size="small">{{ {PENDING:'待处理',RESOLVED:'已处理',REJECTED:'已驳回'}[row.status] }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="170" fixed="right"><template #default="{row}">
          <template v-if="row.status==='PENDING'">
            <el-button text size="small" type="danger" @click="openHandle(row,'resolve')">删除内容</el-button>
            <el-button text size="small" @click="openHandle(row,'reject')">驳回</el-button>
          </template><span v-else class="handled-text">已处理</span>
        </template></el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="dialogVisible" title="处理举报" width="min(92vw, 460px)" class="responsive-dialog">
      <p class="audit-note">目标：{{ current?.targetType }} #{{ current?.targetId }}</p>
      <p class="audit-action" :class="{ danger: action==='resolve' }">{{ action==='resolve'?'将删除被举报内容':'将驳回本次举报' }}</p>
      <el-input v-model="reviewNote" type="textarea" :rows="3" placeholder="审核备注" />
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button :type="action==='resolve'?'danger':'primary'" :loading="handling" @click="handle">确认</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'
const reports=ref([]),status=ref('PENDING'),dialogVisible=ref(false),current=ref(null),action=ref(''),reviewNote=ref(''),handling=ref(false)
const load=async()=>{const r=await request.get('/api/admin/reports',{params:{page:1,size:50,status:status.value}});if(r.code===200) reports.value=r.data.records||[]}
const openHandle=(row,nextAction)=>{current.value=row;action.value=nextAction;reviewNote.value='';dialogVisible.value=true}
const handle=async()=>{handling.value=true;try{const r=await request.put(`/api/admin/reports/${current.value.id}`,{action:action.value,reviewNote:reviewNote.value});if(r.code===200){ElMessage.success('已处理');dialogVisible.value=false;load()}}finally{handling.value=false}}
watch(status,load); onMounted(load)
</script>

<style scoped>
.admin-toolbar .el-select { width: 150px; }
.handled-text { font-size: 12px; color: #b3bdc8; }
.audit-note { color: var(--muted); margin-bottom: 8px; }
.audit-action { margin-bottom: 10px; padding: 10px; border-radius: 12px; background: var(--color-primary-soft); color: var(--color-primary-dark); font-size: 13px; }
.audit-action.danger { background: var(--color-danger-soft); color: var(--color-danger); }
</style>
