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
          <el-button text size="small" type="success" @click="openHandle(row,'resolve')">删除内容</el-button>
          <el-button text size="small" @click="openHandle(row,'reject')">驳回</el-button>
        </template><span v-else style="font-size:12px;color:#ccc">已处理</span>
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" title="处理举报" width="420px">
      <p class="text-muted" style="margin-bottom:8px">目标：{{ current?.targetType }} #{{ current?.targetId }}，动作：{{ action==='resolve'?'删除违规内容':'驳回举报' }}</p>
      <el-input v-model="reviewNote" type="textarea" :rows="3" placeholder="审核备注" />
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handle">确认</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'
const reports=ref([]),status=ref('PENDING'),dialogVisible=ref(false),current=ref(null),action=ref(''),reviewNote=ref('')
const load=async()=>{const r=await request.get('/api/admin/reports',{params:{page:1,size:50,status:status.value}});if(r.code===200) reports.value=r.data.records||[]}
const openHandle=(row,nextAction)=>{current.value=row;action.value=nextAction;reviewNote.value='';dialogVisible.value=true}
const handle=async()=>{await request.put(`/api/admin/reports/${current.value.id}`,{action:action.value,reviewNote:reviewNote.value});ElMessage.success('已处理');dialogVisible.value=false;load()}
watch(status,load); onMounted(load)
</script>

<style scoped>.admin-nav{display:flex;gap:12px;margin-bottom:16px;padding:8px 0;border-bottom:1px solid #eee;font-size:14px}.admin-nav a{color:#555;text-decoration:none}.admin-nav a:hover,.admin-nav a.router-link-active{color:#1a73e8}</style>
