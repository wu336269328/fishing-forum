<template>
  <div>
    <h1 class="page-title">私信</h1>
    <div style="display:grid; grid-template-columns:240px 1fr; gap:12px; min-height:50vh">
      <div class="card" style="padding:8px; overflow-y:auto">
        <div v-for="c in conversations" :key="c.userId" @click="select(c)"
             :style="{padding:'8px',cursor:'pointer',borderRadius:'4px',background:selectedId===c.userId?'#e8f0fe':''}">
          <b style="font-size:13px">{{ c.username }}</b>
          <div style="font-size:12px; color:#999; overflow:hidden; text-overflow:ellipsis; white-space:nowrap">{{ c.lastMessage?.content }}</div>
        </div>
        <el-empty v-if="!conversations.length" description="暂无对话" :image-size="30" />
      </div>
      <div class="card" style="display:flex; flex-direction:column">
        <template v-if="selectedId">
          <div style="padding-bottom:8px; border-bottom:1px solid #eee; font-weight:500; font-size:14px">{{ selectedName }}</div>
          <div ref="chatRef" style="flex:1; overflow-y:auto; padding:12px 0; display:flex; flex-direction:column; gap:8px">
            <div v-for="m in messages" :key="m.id" :style="{alignSelf:m.senderId===userStore.userId?'flex-end':'flex-start',maxWidth:'70%'}">
              <div :style="{padding:'6px 10px',borderRadius:'6px',fontSize:'14px',background:m.senderId===userStore.userId?'#e8f0fe':'#f5f5f5'}">{{ m.content }}</div>
              <div style="font-size:11px; color:#ccc; margin-top:2px">{{ new Date(m.createdAt).toLocaleTimeString('zh-CN',{hour:'2-digit',minute:'2-digit'}) }}</div>
            </div>
          </div>
          <div style="display:flex; gap:6px; padding-top:8px; border-top:1px solid #eee">
            <el-input v-model="newMsg" placeholder="输入消息..." @keyup.enter="send" />
            <el-button type="primary" @click="send">发送</el-button>
          </div>
        </template>
        <div v-else style="display:flex; align-items:center; justify-content:center; flex:1; color:#999">选择对话</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import request from '../api/request'

const userStore = useUserStore()
const conversations = ref([]), messages = ref([]), selectedId = ref(null), selectedName = ref(''), newMsg = ref(''), chatRef = ref(null)

const loadConv = async () => { const r = await request.get('/api/messages'); if(r.code===200) conversations.value=r.data||[] }
const select = async (c) => {
  selectedId.value=c.userId; selectedName.value=c.username
  const r = await request.get(`/api/messages/${c.userId}`,{params:{page:1,size:100}})
  if(r.code===200){ messages.value=(r.data.records||[]).reverse(); await nextTick(); if(chatRef.value) chatRef.value.scrollTop=chatRef.value.scrollHeight }
  loadConv()
}
const send = async () => { if(!newMsg.value.trim()) return; await request.post('/api/messages',{receiverId:selectedId.value,content:newMsg.value}); newMsg.value=''; select({userId:selectedId.value,username:selectedName.value}) }
onMounted(loadConv)
</script>
