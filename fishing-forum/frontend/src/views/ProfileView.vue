<template>
  <div v-if="profile">
    <!-- 用户信息卡 -->
    <div class="card profile-card">
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
            <div class="stat-item"><div class="stat-num">{{ profile.followerCount||0 }}</div><div class="stat-label">粉丝</div></div>
            <div class="stat-item"><div class="stat-num">{{ profile.followingCount||0 }}</div><div class="stat-label">关注</div></div>
          </div>
        </div>
        <div v-if="!isOwn && userStore.isLoggedIn" style="align-self:flex-start">
          <el-button :type="isFollowing?'':'primary'" size="small" @click="toggleFollow">{{ isFollowing?'已关注':'+ 关注' }}</el-button>
          <el-button size="small" @click="$router.push('/messages')">私信</el-button>
        </div>
      </div>
    </div>

    <!-- 编辑资料 -->
    <div v-if="isOwn" class="card">
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

    <!-- 帖子 -->
    <div style="display:flex; justify-content:space-between; align-items:center; margin:16px 0 8px">
      <h3 style="font-size:15px">📝 {{ isOwn ? '我的帖子' : 'TA的帖子' }}</h3>
    </div>
    <div v-for="p in userPosts" :key="p.id" class="card" style="cursor:pointer" @click="$router.push(`/post/${p.id}`)">
      <div style="font-size:15px; font-weight:500; margin-bottom:4px">{{ p.title }}</div>
      <div class="post-meta">
        <span>{{ new Date(p.createdAt).toLocaleDateString('zh-CN') }}</span>
        <span>👁 {{ p.viewCount }}</span>
        <span>💬 {{ p.commentCount }}</span>
        <span>👍 {{ p.likeCount }}</span>
      </div>
    </div>
    <el-empty v-if="!userPosts.length" description="暂无帖子" :image-size="40" />
  </div>
  <div v-else class="card" style="text-align:center; padding:40px">
    <p style="font-size:15px; color:#666; margin-bottom:12px">请先登录查看个人中心</p>
    <router-link to="/login"><el-button type="primary">去登录</el-button></router-link>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const route = useRoute(), userStore = useUserStore()
const profile = ref(null), userPosts = ref([]), isFollowing = ref(false)
const editForm = ref({ bio: '', email: '' })
const pwForm = ref({ oldPassword: '', newPassword: '' })
const fileInput = ref(null)
const isOwn = computed(() => { const id = route.params.id ? Number(route.params.id) : userStore.userId; return id === userStore.userId })

const load = async () => {
  const id = route.params.id || userStore.userId
  if (!id) { profile.value = null; return }
  const r = await request.get(`/api/users/${id}/profile`); if (r.code === 200) { profile.value = r.data; editForm.value = { bio: r.data.bio || '', email: r.data.email || '' } }
  const p = await request.get(`/api/users/${id}/posts`, { params: { page: 1, size: 20 } }); if (p.code === 200) userPosts.value = p.data.records || p.data || []
  if (!isOwn.value && userStore.isLoggedIn) { try { const f = await request.get(`/api/follows/check/${id}`); if (f.code === 200) isFollowing.value = f.data } catch (e) { } }
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
const toggleFollow = async () => { const r = await request.post(`/api/follows/${profile.value.id}`); if (r.code === 200) { isFollowing.value = !isFollowing.value; ElMessage.success(r.message) } }
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
.avatar-wrap { position: relative; cursor: pointer; flex-shrink: 0; }
.avatar-overlay { position: absolute; inset: 0; border-radius: 50%; background: rgba(0,0,0,0.4); color: #fff; font-size: 12px; display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.2s; }
.avatar-wrap:hover .avatar-overlay { opacity: 1; }
.profile-info { flex: 1; }
.profile-stats { display: flex; gap: 20px; }
.stat-item { text-align: center; }
.stat-num { font-size: 18px; font-weight: 700; color: #1a73e8; }
.stat-label { font-size: 11px; color: #999; }
.post-meta { font-size: 12px; color: #999; display: flex; gap: 12px; }
</style>
