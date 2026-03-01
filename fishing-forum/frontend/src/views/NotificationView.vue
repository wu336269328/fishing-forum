<template>
  <div>
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px">
      <h1 class="page-title" style="margin:0">通知</h1>
      <el-button text size="small" @click="markAllRead" v-if="notifications.length">全部已读</el-button>
    </div>
    <div v-for="n in notifications" :key="n.id" class="card" :style="{borderLeft: n.isRead?'':'3px solid #1a73e8', cursor:'pointer'}" @click="markRead(n)">
      <div style="font-size:14px; font-weight:500">{{ n.title }}</div>
      <div style="font-size:13px; color:#777">{{ n.content }}</div>
      <div style="font-size:12px; color:#ccc; margin-top:4px">{{ new Date(n.createdAt).toLocaleString('zh-CN') }}</div>
    </div>
    <el-empty v-if="!notifications.length" description="暂无通知" :image-size="40" />
    <div class="pagination-wrap" v-if="total>20"><el-pagination background layout="prev,pager,next" :total="total" :page-size="20" v-model:current-page="page" @current-change="load" /></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const notifications = ref([]), page = ref(1), total = ref(0)
const load = async () => { const r = await request.get('/api/notifications',{params:{page:page.value,size:20}}); if(r.code===200){ notifications.value=r.data.records||[]; total.value=r.data.total||0 } }
const markRead = async (n) => { if(!n.isRead){ await request.put(`/api/notifications/${n.id}/read`); n.isRead=true } }
const markAllRead = async () => { await request.put('/api/notifications/read-all'); notifications.value.forEach(n=>n.isRead=true); ElMessage.success('全部已读') }
onMounted(load)
</script>
