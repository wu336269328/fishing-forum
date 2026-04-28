<template>
  <header class="header">
    <div class="header-inner">
      <router-link to="/" class="logo">
        <span class="logo-mark">🎣</span>
        <span><b>钓友圈</b><small>Fishing Forum</small></span>
      </router-link>
      <nav class="nav desktop-nav">
        <router-link v-for="item in navItems" :key="item.to" :to="item.to">{{ item.label }}</router-link>
      </nav>
      <div class="view-switch" role="group" aria-label="网页视图切换">
        <button v-for="option in viewOptions" :key="option.value" type="button" :class="{ active: viewMode.mode === option.value }" @click="viewMode.setMode(option.value)">
          {{ option.label }}
        </button>
      </div>
      <div class="actions">
        <template v-if="userStore.isLoggedIn">
          <router-link to="/post/create"><el-button type="primary" size="small" round>发帖</el-button></router-link>
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
          <router-link to="/login"><el-button size="small" round>登录</el-button></router-link>
          <router-link to="/register"><el-button type="primary" size="small" round>注册</el-button></router-link>
        </template>
      </div>
      <button class="menu-button" type="button" @click="mobileMenuOpen = !mobileMenuOpen" aria-label="打开菜单">☰</button>
    </div>
    <div v-if="mobileMenuOpen" class="mobile-panel">
      <router-link v-for="item in navItems" :key="item.to" :to="item.to" @click="mobileMenuOpen=false">{{ item.label }}</router-link>
      <router-link v-if="userStore.isLoggedIn" to="/post/create" @click="mobileMenuOpen=false">📝 发帖</router-link>
      <router-link v-if="userStore.isAdmin" to="/admin" @click="mobileMenuOpen=false">⚙️ 管理后台</router-link>
      <button v-if="userStore.isLoggedIn" type="button" @click="handleLogout">退出登录</button>
      <router-link v-else to="/login" @click="mobileMenuOpen=false">登录 / 注册</router-link>
    </div>
  </header>
  <nav class="mobile-tabbar">
    <router-link to="/">首页</router-link>
    <router-link to="/forum">论坛</router-link>
    <router-link class="create-tab" to="/post/create">发帖</router-link>
    <router-link :to="userStore.isLoggedIn ? '/messages' : '/login'">{{ userStore.isLoggedIn ? '消息' : '登录' }}</router-link>
    <router-link :to="userStore.isLoggedIn ? '/profile' : '/register'">{{ userStore.isLoggedIn ? '我的' : '注册' }}</router-link>
  </nav>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useViewModeStore } from '../../stores/viewMode'
import request from '../../api/request'

const router = useRouter(), userStore = useUserStore()
const unread = ref(0)
const mobileMenuOpen = ref(false)
const viewMode = useViewModeStore()
const viewOptions = [
  { label: '自动', value: 'auto' },
  { label: 'PC', value: 'desktop' },
  { label: '手机', value: 'mobile' }
]
const navItems = [
  { to: '/', label: '首页' },
  { to: '/forum', label: '论坛' },
  { to: '/spots', label: '钓点' },
  { to: '/wiki', label: '百科' },
  { to: '/weather', label: '天气' },
]

const handleLogout = () => { userStore.logout(); mobileMenuOpen.value = false; router.push('/') }

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
.header { background: rgba(255,255,255,0.92); border-bottom: 1px solid var(--line); position: sticky; top: 0; z-index: 100; backdrop-filter: blur(14px); }
.header-inner { max-width: 1440px; margin: 0 auto; padding: 0 24px; min-height: 64px; display: flex; align-items: center; gap: 18px; }
.logo { font-size: 16px; color: var(--ink); text-decoration: none; flex-shrink: 0; display: flex; align-items: center; gap: 10px; }
.logo-mark { width: 36px; height: 36px; border-radius: 14px; background: linear-gradient(135deg, #365f93, #4f7fbf); color: #fff; display: grid; place-items: center; box-shadow: var(--shadow-soft); }
.logo small { display: block; font-size: 10px; font-weight: 600; color: var(--muted); letter-spacing: .08em; text-transform: uppercase; line-height: 1.1; }
.nav { display: flex; gap: 6px; flex: 1; }
.nav a { color: #4b5563; font-size: 14px; text-decoration: none; padding: 8px 12px; border-radius: 999px; }
.nav a:hover, .nav a.router-link-active { color: var(--green); background: var(--green-soft); }
.actions { display: flex; align-items: center; gap: 12px; flex-shrink: 0; font-size: 14px; }
.icon-link { position: relative; text-decoration: none; font-size: 16px; }
.badge { position: absolute; top: -6px; right: -8px; background: #e74c3c; color: #fff; font-size: 10px; padding: 0 4px; border-radius: 8px; line-height: 16px; }
.user-trigger { display: flex; align-items: center; gap: 6px; cursor: pointer; }
.user-name { font-size: 14px; color: #333; }
.view-switch { flex-shrink: 0; display: inline-flex; align-items: center; gap: 2px; padding: 3px; border: 1px solid var(--line); border-radius: 999px; background: rgba(255,255,255,.82); }
.view-switch button { border: 0; background: transparent; color: var(--muted); border-radius: 999px; padding: 5px 9px; font-size: 12px; cursor: pointer; }
.view-switch button.active { background: var(--green); color: #fff; font-weight: 700; }
.menu-button { display: none; border: 1px solid var(--line); background: #fff; border-radius: 12px; width: 38px; height: 36px; font-size: 18px; }
.mobile-panel { display: none; }
.mobile-tabbar { display: none; }

@media (max-width: 768px) {
  .desktop-nav, .actions { display: none; }
  .header-inner { min-height: 60px; padding: 0 16px; gap: 10px; }
  .logo small { display: none; }
  .view-switch { display: none; }
  .logo { font-size: 15px; }
  .logo-mark { width: 34px; height: 34px; border-radius: 13px; }
  .menu-button { display: block; }
  .mobile-panel { display: grid; gap: 6px; padding: 10px 14px 14px; background: rgba(255,255,255,0.96); border-top: 1px solid var(--line); box-shadow: var(--shadow-soft); }
  .mobile-panel a, .mobile-panel button { border: 0; border-radius: 12px; padding: 12px 14px; background: var(--sand); color: var(--ink); text-align: left; font-size: 14px; }
  .mobile-tabbar { position: fixed; left: max(10px, env(safe-area-inset-left)); right: max(10px, env(safe-area-inset-right)); bottom: max(10px, env(safe-area-inset-bottom)); width: auto; transform: none; z-index: 120; display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); align-items: center; gap: 4px; padding: 8px; border: 1px solid var(--line); border-radius: 22px; background: rgba(255,255,255,.94); backdrop-filter: blur(14px); box-shadow: 0 18px 40px rgba(30, 41, 59, .16); }
  .mobile-tabbar a { color: #4b5563; font-size: 12px; text-align: center; padding: 7px 2px; border-radius: 14px; }
  .mobile-tabbar a.router-link-active { color: var(--green); background: var(--green-soft); font-weight: 700; }
  .mobile-tabbar .create-tab { color: #fff; background: var(--green); font-weight: 700; transform: translateY(-6px); box-shadow: 0 10px 22px rgba(79, 127, 191, .25); }
}

</style>
