<template>
  <div v-if="profile" class="page-shell">
    <!-- 用户信息卡 -->
    <div class="card profile-card hero-profile">
      <div class="profile-top">
        <div class="avatar-wrap" @click="isOwn && triggerUpload()">
          <img :src="profile.avatar || '/default-avatar.png'" class="avatar-lg" />
          <div v-if="isOwn" class="avatar-overlay">更换</div>
          <input v-if="isOwn" ref="fileInput" type="file" accept="image/*" style="display:none" @change="uploadAvatar" />
        </div>
        <div class="profile-info">
          <h2 style="font-size:18px; margin-bottom:2px">{{ profile.username }}
            <el-tag v-if="profile.role==='ADMIN'" type="danger" size="small" style="margin-left:6px">管理员</el-tag>
          </h2>
          <p style="font-size:13px; color:#777; margin-bottom:8px">{{ profile.bio || '这个人很懒，什么也没留下' }}</p>
          <div class="profile-stats">
            <div class="stat-item"><div class="stat-num">{{ profile.postCount||0 }}</div><div class="stat-label">帖子</div></div>
            <div class="stat-item" style="cursor:pointer" @click="showFollowDialog('followers')"><div class="stat-num">{{ profile.followerCount||0 }}</div><div class="stat-label">粉丝</div></div>
            <div class="stat-item" style="cursor:pointer" @click="showFollowDialog('followings')"><div class="stat-num">{{ profile.followingCount||0 }}</div><div class="stat-label">关注</div></div>
          </div>
        </div>
        <div v-if="!isOwn && userStore.isLoggedIn" class="profile-actions">
          <el-button :type="isFollowing?'':'primary'" size="small" @click="toggleFollow">{{ isFollowing?'已关注':'+ 关注' }}</el-button>
          <el-button size="small" @click="startChat">✉️ 私信</el-button>
        </div>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <div :class="['tab-item', {active: activeTab==='posts'}]" @click="activeTab='posts'">📝 {{ isOwn ? '我的帖子' : 'TA的帖子' }}</div>
      <div v-if="isOwn" :class="['tab-item', {active: activeTab==='growth'}]" @click="activeTab='growth'; loadGrowth()">🏅 成长</div>
      <div v-if="isOwn" :class="['tab-item', {active: activeTab==='favorites'}]" @click="activeTab='favorites'; loadFavorites()">⭐ 我的收藏</div>
      <div v-if="isOwn" :class="['tab-item', {active: activeTab==='edit'}]" @click="activeTab='edit'">✏️ 编辑资料</div>
    </div>

    <!-- 帖子列表 -->
    <template v-if="activeTab==='posts'">
      <div v-for="p in userPosts" :key="p.id" class="card list-card" style="cursor:pointer" @click="$router.push(`/post/${p.id}`)">
        <div style="font-size:15px; font-weight:500; margin-bottom:4px">{{ p.title }}</div>
        <div class="post-meta">
          <span>{{ new Date(p.createdAt).toLocaleDateString('zh-CN') }}</span>
          <span>👁 {{ p.viewCount }}</span>
          <span>💬 {{ p.commentCount }}</span>
          <span>👍 {{ p.likeCount }}</span>
        </div>
      </div>
      <el-empty v-if="!userPosts.length" description="暂无帖子" :image-size="40" />
    </template>

    <!-- 成长信息 -->
    <template v-if="activeTab==='growth'">
      <div class="card growth-card">
        <div>
          <div class="growth-level">Lv.{{ growth.level || 1 }}</div>
          <div class="text-muted">积分 {{ growth.points || 0 }} / {{ growth.nextLevelPoints || 100 }}</div>
        </div>
        <el-progress :percentage="growthProgress" :stroke-width="10" />
        <div class="growth-grid">
          <div><b>{{ growth.posts || 0 }}</b><span>发帖</span></div>
          <div><b>{{ growth.followers || 0 }}</b><span>粉丝</span></div>
          <div><b>{{ growth.following || 0 }}</b><span>关注</span></div>
        </div>
        <div class="badge-row">
          <el-tag v-for="b in growth.badges || []" :key="b" effect="plain">{{ b }}</el-tag>
          <el-empty v-if="!(growth.badges || []).length" description="继续发帖互动解锁徽章" :image-size="32" />
        </div>
      </div>
    </template>

    <!-- 收藏列表 -->
    <template v-if="activeTab==='favorites'">
      <div v-for="f in favorites" :key="f.id" class="card" style="cursor:pointer" @click="$router.push(`/post/${f.postId || f.id}`)">
        <div style="font-size:15px; font-weight:500; margin-bottom:4px">{{ f.postTitle || f.title }}</div>
        <div class="post-meta">
          <span>收藏于 {{ new Date(f.createdAt).toLocaleDateString('zh-CN') }}</span>
          <span v-if="f.authorName">by {{ f.authorName }}</span>
        </div>
      </div>
      <el-empty v-if="!favorites.length" description="暂无收藏" :image-size="40" />
    </template>

    <!-- 编辑资料 -->
    <template v-if="activeTab==='edit'">
      <div class="card">
        <h3 style="font-size:14px; margin-bottom:10px">✏️ 编辑资料</h3>
        <el-form :model="editForm" label-width="60px" size="small" style="max-width:420px">
          <el-form-item label="简介"><el-input v-model="editForm.bio" placeholder="一句话介绍自己" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="editForm.email" /></el-form-item>
          <el-form-item><el-button type="primary" @click="saveProfile">保存修改</el-button></el-form-item>
        </el-form>
        <hr class="section-divider" />
        <h3 style="font-size:14px; margin-bottom:10px">🔒 修改密码</h3>
        <el-form :model="pwForm" label-width="80px" size="small" style="max-width:420px">
          <el-form-item label="原密码"><el-input v-model="pwForm.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwForm.newPassword" type="password" show-password /></el-form-item>
          <el-form-item><el-button type="primary" @click="changePassword">修改密码</el-button></el-form-item>
        </el-form>
      </div>
    </template>

    <!-- 关注/粉丝弹窗 -->
    <el-dialog v-model="followDialogVisible" :title="followDialogType==='followers'?'粉丝列表':'关注列表'" width="400px">
      <div v-for="u in followList" :key="u.id" style="display:flex; align-items:center; gap:10px; padding:8px 0; border-bottom:1px solid #f5f5f5; cursor:pointer" @click="followDialogVisible=false; $router.push(`/profile/${u.id}`)">
        <img :src="u.avatar || '/default-avatar.png'" class="avatar-sm" />
        <div style="flex:1">
          <div style="font-weight:500; font-size:14px">{{ u.username }}</div>
          <div class="text-muted">{{ u.bio || '' }}</div>
        </div>
      </div>
      <el-empty v-if="!followList.length" :description="followDialogType==='followers'?'暂无粉丝':'暂无关注'" :image-size="30" />
    </el-dialog>

    <!-- 私信弹窗 -->
    <el-dialog v-model="chatDialogVisible" :title="`私信 ${profile.username}`" width="450px">
      <div ref="chatRef" style="max-height:300px; overflow-y:auto; padding:8px 0; display:flex; flex-direction:column; gap:8px">
        <div v-for="m in chatMessages" :key="m.id" :style="{alignSelf:m.senderId===userStore.userId?'flex-end':'flex-start',maxWidth:'70%'}">
          <div :style="{padding:'6px 10px',borderRadius:'6px',fontSize:'14px',background:m.senderId===userStore.userId?'#e8f0fe':'#f5f5f5'}">{{ m.content }}</div>
          <div style="font-size:11px; color:#ccc; margin-top:2px">{{ new Date(m.createdAt).toLocaleTimeString('zh-CN',{hour:'2-digit',minute:'2-digit'}) }}</div>
        </div>
        <el-empty v-if="!chatMessages.length" description="暂无消息，打个招呼吧" :image-size="24" />
      </div>
      <div style="display:flex; gap:6px; margin-top:12px">
        <el-input v-model="chatInput" placeholder="输入消息..." @keyup.enter="sendChat" />
        <el-button type="primary" @click="sendChat" :disabled="!chatInput.trim()">发送</el-button>
      </div>
    </el-dialog>
  </div>
  <div v-else-if="loadError" class="page-shell">
    <div class="card desktop-error-card">
      <div>
        <b>个人中心加载失败</b>
        <p class="text-muted">当前无法获取用户资料，请检查后端服务或稍后重试。</p>
      </div>
      <el-button size="small" type="primary" @click="load">重新加载</el-button>
    </div>
  </div>
  <div v-else class="card" style="text-align:center; padding:40px">
    <p style="font-size:15px; color:#666; margin-bottom:12px">请先登录查看个人中心</p>
    <router-link to="/login"><el-button type="primary">去登录</el-button></router-link>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const route = useRoute(), router = useRouter(), userStore = useUserStore()
const profile = ref(null), userPosts = ref([]), favorites = ref([]), isFollowing = ref(false)
const loadError = ref(false)
const growth = ref({})
const editForm = ref({ bio: '', email: '' })
const pwForm = ref({ oldPassword: '', newPassword: '' })
const fileInput = ref(null)
const activeTab = ref('posts')
const followDialogVisible = ref(false), followDialogType = ref('followers'), followList = ref([])
const chatDialogVisible = ref(false), chatMessages = ref([]), chatInput = ref(''), chatRef = ref(null)

const isOwn = computed(() => { const id = route.params.id ? Number(route.params.id) : userStore.userId; return id === userStore.userId })

const load = async () => {
  loadError.value = false
  const id = route.params.id || userStore.userId
  if (!id) { profile.value = null; return }
  activeTab.value = 'posts'
  try {
    const r = await request.get(`/api/users/${id}/profile`); if (r.code === 200) { profile.value = r.data; editForm.value = { bio: r.data.bio || '', email: r.data.email || '' } }
    const p = await request.get(`/api/users/${id}/posts`, { params: { page: 1, size: 20 } }); if (p.code === 200) userPosts.value = p.data.records || p.data || []
    if (!isOwn.value && userStore.isLoggedIn) { try { const f = await request.get(`/api/follows/check/${id}`); if (f.code === 200) isFollowing.value = f.data } catch (e) { } }
  } catch (e) {
    loadError.value = true
  }
}

const loadFavorites = async () => {
  const r = await request.get('/api/favorites')
  if (r.code === 200) favorites.value = r.data || []
}

const growthProgress = computed(() => {
  const next = growth.value.nextLevelPoints || 100
  return Math.min(100, Math.round(((growth.value.points || 0) / next) * 100))
})

const loadGrowth = async () => {
  const r = await request.get('/api/users/me/growth')
  if (r.code === 200) growth.value = r.data || {}
}

const triggerUpload = () => fileInput.value?.click()
const uploadAvatar = async (e) => {
  const file = e.target.files[0]; if (!file) return
  if (file.size > 5 * 1024 * 1024) return ElMessage.warning('图片不超过5MB')
  const fd = new FormData(); fd.append('file', file)
  try {
    const r = await request.post('/api/users/me/avatar', fd)
    if (r.code === 200) { ElMessage.success('头像已更新'); load(); userStore.fetchCurrentUser() }
    else ElMessage.error(r.message || '上传失败')
  } catch (err) { ElMessage.error('上传失败: ' + (err.response?.data?.message || err.message)) }
  e.target.value = ''
}

const toggleFollow = async () => {
  const r = await request.post(`/api/follows/${profile.value.id}`)
  if (r.code === 200) {
    isFollowing.value = !isFollowing.value
    profile.value.followerCount += isFollowing.value ? 1 : -1
    ElMessage.success(r.message)
  }
}

const showFollowDialog = async (type) => {
  followDialogType.value = type
  followDialogVisible.value = true
  const id = route.params.id || userStore.userId
  const url = type === 'followers' ? `/api/users/${id}/followers` : `/api/users/${id}/followings`
  const r = await request.get(url)
  if (r.code === 200) followList.value = r.data || []
}

const startChat = async () => {
  chatDialogVisible.value = true
  chatInput.value = ''
  const r = await request.get(`/api/messages/${profile.value.id}`, { params: { page: 1, size: 50 } })
  if (r.code === 200) {
    chatMessages.value = (r.data.records || r.data || []).reverse()
    await nextTick()
    if (chatRef.value) chatRef.value.scrollTop = chatRef.value.scrollHeight
  }
}

const sendChat = async () => {
  if (!chatInput.value.trim()) return
  await request.post('/api/messages', { receiverId: profile.value.id, content: chatInput.value })
  chatInput.value = ''
  startChat()
}

const saveProfile = async () => { const r = await request.put('/api/users/me', editForm.value); if (r.code === 200) { ElMessage.success('已保存'); userStore.fetchCurrentUser() } }
const changePassword = async () => {
  if (!pwForm.value.oldPassword || !pwForm.value.newPassword) return ElMessage.warning('请填写密码')
  if (pwForm.value.newPassword.length < 6) return ElMessage.warning('新密码至少6位')
  const r = await request.put('/api/users/me/password', pwForm.value)
  if (r.code === 200) { ElMessage.success('密码已修改'); pwForm.value = { oldPassword: '', newPassword: '' } }
  else ElMessage.error(r.message)
}

onMounted(load)
watch(() => route.params.id, load)
</script>

<style scoped>
.profile-top { display: flex; gap: 20px; align-items: flex-start; flex-wrap: wrap; }
.hero-profile { background: linear-gradient(135deg, rgba(255,253,248,.92), rgba(231,243,236,.92)); }
.avatar-wrap { position: relative; cursor: pointer; flex-shrink: 0; }
.avatar-overlay { position: absolute; inset: 0; border-radius: 50%; background: rgba(0,0,0,0.4); color: #fff; font-size: 12px; display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.2s; }
.avatar-wrap:hover .avatar-overlay { opacity: 1; }
.profile-info { flex: 1; }
.profile-stats { display: flex; gap: 20px; }
.profile-actions { align-self: flex-start; display: flex; gap: 8px; }
.stat-item { text-align: center; }
.stat-num { font-size: 18px; font-weight: 700; color: #1a73e8; }
.stat-label { font-size: 11px; color: #999; }
.post-meta { font-size: 12px; color: #999; display: flex; gap: 12px; }
.tab-bar { display: flex; gap: 0; margin: 16px 0 12px; border-bottom: 2px solid #eee; }
.tab-item { padding: 8px 16px; font-size: 14px; cursor: pointer; color: #666; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all 0.2s; }
.tab-item:hover { color: #1a73e8; }
.tab-item.active { color: #1a73e8; border-bottom-color: #1a73e8; font-weight: 500; }
.growth-card { display: grid; gap: 14px; }
.growth-level { font-size: 30px; font-weight: 900; color: var(--blue); }
.growth-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.growth-grid div { border: 1px solid var(--line); border-radius: 14px; padding: 12px; text-align: center; background: var(--green-soft); }
.growth-grid b { display: block; font-size: 20px; color: var(--ink); }
.growth-grid span { font-size: 12px; color: var(--muted); }
.badge-row { display: flex; gap: 8px; flex-wrap: wrap; }
@media (max-width: 768px) {
  .profile-top { align-items: center; }
  .profile-info { min-width: 100%; }
  .profile-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
  .profile-actions { width: 100%; }
  .profile-actions .el-button { flex: 1; }
  .tab-bar { overflow-x: auto; }
  .tab-item { flex-shrink: 0; }
}
</style>
