<template>
  <div class="page-shell message-page">
    <h1 class="page-title">私信</h1>
    <div class="message-layout" :class="{ 'chat-open': selectedId }">
      <div class="card conversation-panel">
        <div class="panel-title">会话</div>
        <div
          v-for="c in conversations"
          :key="c.userId"
          class="conversation-item"
          :class="{ active: selectedId===c.userId }"
          role="button"
          tabindex="0"
          @click="select(c)"
          @keydown.enter.space.prevent="select(c)"
        >
          <b>{{ c.username }}</b>
          <div>{{ c.lastMessage?.content || '暂无消息内容' }}</div>
        </div>
        <div v-if="!conversations.length" class="empty-state">
          <div class="empty-state-icon">💬</div>
          <div class="empty-state-title">还没有私信对话</div>
          <div class="empty-state-text">在帖子或个人主页上点「私信」开始聊天。</div>
        </div>
      </div>
      <div class="card chat-panel">
        <template v-if="selectedId">
          <div class="chat-header">
            <button type="button" class="back-to-conversations" @click="selectedId=null">← 返回</button>
            <span>{{ selectedName }}</span>
          </div>
          <div ref="chatRef" class="message-list">
            <div v-for="m in messages" :key="m.id" class="message-bubble-wrap" :class="{ mine: m.senderId===userStore.userId }">
              <div class="message-bubble">{{ m.content }}</div>
              <div class="message-time">{{ new Date(m.createdAt).toLocaleTimeString('zh-CN',{hour:'2-digit',minute:'2-digit'}) }}</div>
            </div>
          </div>
          <div class="message-input-bar">
            <el-input v-model="newMsg" placeholder="输入消息..." @keyup.enter="send" />
            <el-button type="primary" @click="send" :disabled="!newMsg.trim()">发送</el-button>
          </div>
        </template>
        <div v-else class="chat-empty">
          <div class="empty-state-icon">📬</div>
          <div class="empty-state-text">选择左侧对话开始聊天</div>
        </div>
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
const loadError = ref(false)

const loadConv = async () => {
  loadError.value = false
  try {
    const r = await request.get('/api/messages')
    if(r.code===200) conversations.value=r.data||[]
  } catch (e) {
    loadError.value = true
  }
}
const select = async (c) => {
  selectedId.value=c.userId; selectedName.value=c.username
  const r = await request.get(`/api/messages/${c.userId}`,{params:{page:1,size:100}})
  if(r.code===200){ messages.value=(r.data.records||[]).reverse(); await nextTick(); if(chatRef.value) chatRef.value.scrollTop=chatRef.value.scrollHeight }
  loadConv()
}
const send = async () => { if(!newMsg.value.trim()) return; await request.post('/api/messages',{receiverId:selectedId.value,content:newMsg.value}); newMsg.value=''; select({userId:selectedId.value,username:selectedName.value}) }
onMounted(loadConv)
</script>

<style scoped>
.message-page { min-width: 0; }
.message-layout { display: grid; grid-template-columns: 280px minmax(0, 1fr); gap: 12px; min-height: 58vh; }
.conversation-panel { padding: 10px; overflow-y: auto; }
.panel-title { padding: 4px 6px 8px; color: var(--muted); font-size: 12px; font-weight: 800; }
.conversation-item { padding: 10px; cursor: pointer; border-radius: 14px; transition: background .15s; }
.conversation-item:hover, .conversation-item.active { background: var(--color-primary-soft); }
.conversation-item b { display: block; color: var(--ink); font-size: 14px; }
.conversation-item div { color: #8b98a8; font-size: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.chat-panel { display: flex; flex-direction: column; min-height: 58vh; }
.chat-header { display: flex; align-items: center; gap: 10px; padding-bottom: 10px; border-bottom: 1px solid #edf2f7; font-weight: 800; color: var(--ink); }
.back-to-conversations { display: none; border: 0; border-radius: 999px; padding: 8px 14px; min-height: 36px; color: var(--color-primary); background: var(--color-primary-soft); font-size: 13px; cursor: pointer; font-family: inherit; }
.message-list { flex: 1; overflow-y: auto; padding: 14px 0; display: flex; flex-direction: column; gap: 10px; }
.message-bubble-wrap { align-self: flex-start; max-width: 72%; }
.message-bubble-wrap.mine { align-self: flex-end; }
.message-bubble { padding: 9px 12px; border-radius: 16px 16px 16px 4px; background: var(--surface-muted); color: var(--ink); font-size: 14px; line-height: 1.5; }
.message-bubble-wrap.mine .message-bubble { border-radius: 16px 16px 4px 16px; background: var(--color-primary); color: #fff; }
.message-time { margin-top: 2px; color: #b3bdc8; font-size: 11px; }
.message-bubble-wrap.mine .message-time { text-align: right; }
.message-input-bar { display: flex; gap: 8px; padding-top: 10px; border-top: 1px solid #edf2f7; }
.chat-empty { flex: 1; display: grid; place-items: center; color: var(--muted); text-align: center; }
.chat-empty .empty-state-icon { font-size: 36px; margin-bottom: 8px; }
@media (max-width: 768px) {
  .message-layout { display: block; min-height: auto; }
  .conversation-panel { display: block; }
  .chat-panel { display: none; min-height: calc(100vh - 190px); }
  .message-layout.chat-open .conversation-panel { display: none; }
  .message-layout.chat-open .chat-panel { display: flex; }
  .back-to-conversations { display: inline-flex; }
  .message-input-bar { position: sticky; bottom: calc(var(--tabbar-offset) + 12px); background: rgba(255,255,255,.96); padding-top: 10px; }
  .message-bubble-wrap { max-width: 84%; }
}
</style>
