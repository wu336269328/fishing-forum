<template>
  <header class="header">
    <div class="header-inner">
      <router-link to="/" class="logo">🎣 钓友圈</router-link>
      <nav class="nav">
        <router-link to="/">首页</router-link>
        <router-link to="/forum">论坛</router-link>
        <router-link to="/spots">钓点</router-link>
        <router-link to="/wiki">百科</router-link>
        <router-link to="/weather">天气</router-link>
      </nav>
      <div class="actions">
        <template v-if="userStore.isLoggedIn">
          <router-link to="/post/create"><el-button type="primary" size="small">发帖</el-button></router-link>
          <router-link to="/notifications" class="icon-link" title="通知">🔔<span v-if="unread" class="badge">{{ unread }}</span></router-link>
          <router-link to="/messages" class="icon-link" title="私信">✉️</router-link>
          <el-dropdown trigger="click">
            <div class="user-trigger">
              <img :src="userStore.user?.avatar || '/default-avatar.png'" class="avatar-sm" />
              <span class="user-name">{{ userStore.user?.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">👤 个人中心</el-dropdown-item>
                <el-dropdown-item @click="$router.push('/messages')">✉️ 我的私信</el-dropdown-item>
                <el-dropdown-item @click="$router.push('/notifications')">🔔 通知</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" divided @click="$router.push('/admin')">⚙️ 管理后台</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">🚪 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login"><el-button size="small">登录</el-button></router-link>
          <router-link to="/register"><el-button type="primary" size="small">注册</el-button></router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import request from '../../api/request'

const router = useRouter(), userStore = useUserStore()
const unread = ref(0)

const handleLogout = () => { userStore.logout(); router.push('/') }

onMounted(async () => {
  if (userStore.isLoggedIn) {
    try {
      const r = await request.get('/api/notifications/unread-count')
      if (r.code === 200) {
        // API返回对象 {total, messages, notifications} 或直接数字
        const d = r.data
        unread.value = typeof d === 'number' ? d : (d?.total || 0)
      }
    } catch (e) {}
  }
})
</script>

<style scoped>
.header { background: #fff; border-bottom: 1px solid #e0e0e0; position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 1000px; margin: 0 auto; padding: 0 16px; height: 52px; display: flex; align-items: center; gap: 20px; }
.logo { font-weight: 700; font-size: 16px; color: #111; text-decoration: none; flex-shrink: 0; }
.nav { display: flex; gap: 16px; flex: 1; }
.nav a { color: #555; font-size: 14px; text-decoration: none; padding: 4px 0; }
.nav a:hover, .nav a.router-link-active { color: #1a73e8; }
.actions { display: flex; align-items: center; gap: 12px; flex-shrink: 0; font-size: 14px; }
.icon-link { position: relative; text-decoration: none; font-size: 16px; }
.badge { position: absolute; top: -6px; right: -8px; background: #e74c3c; color: #fff; font-size: 10px; padding: 0 4px; border-radius: 8px; line-height: 16px; }
.user-trigger { display: flex; align-items: center; gap: 6px; cursor: pointer; }
.user-name { font-size: 14px; color: #333; }
@media (max-width: 768px) { .nav { display: none; } }
</style>
