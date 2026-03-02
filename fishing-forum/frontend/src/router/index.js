import { createRouter, createWebHistory } from 'vue-router'

// 路由配置
const routes = [
    { path: '/', name: 'Home', component: () => import('../views/HomeView.vue'), meta: { title: '首页' } },
    { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue'), meta: { title: '登录' } },
    { path: '/register', name: 'Register', component: () => import('../views/RegisterView.vue'), meta: { title: '注册' } },
    { path: '/forum', name: 'Forum', component: () => import('../views/ForumView.vue'), meta: { title: '论坛' } },
    { path: '/post/:id', name: 'PostDetail', component: () => import('../views/PostDetailView.vue'), meta: { title: '帖子详情' } },
    { path: '/post/create', name: 'CreatePost', component: () => import('../views/CreatePostView.vue'), meta: { title: '发帖', auth: true } },
    { path: '/post/edit/:id', name: 'EditPost', component: () => import('../views/EditPostView.vue'), meta: { title: '编辑帖子', auth: true } },
    { path: '/profile/:id?', name: 'Profile', component: () => import('../views/ProfileView.vue'), meta: { title: '个人中心' } },
    { path: '/spots', name: 'Spots', component: () => import('../views/SpotMapView.vue'), meta: { title: '钓点地图' } },
    { path: '/wiki', name: 'Wiki', component: () => import('../views/WikiView.vue'), meta: { title: '知识百科' } },
    { path: '/wiki/:id', name: 'WikiDetail', component: () => import('../views/WikiDetailView.vue'), meta: { title: '词条详情' } },
    { path: '/weather', name: 'Weather', component: () => import('../views/WeatherView.vue'), meta: { title: '天气查询' } },
    { path: '/messages', name: 'Messages', component: () => import('../views/MessageView.vue'), meta: { title: '私信', auth: true } },
    { path: '/notifications', name: 'Notifications', component: () => import('../views/NotificationView.vue'), meta: { title: '通知', auth: true } },
    // 后台管理
    { path: '/admin', name: 'Admin', component: () => import('../views/admin/DashboardView.vue'), meta: { title: '管理后台', auth: true, admin: true } },
    { path: '/admin/users', name: 'AdminUsers', component: () => import('../views/admin/UserManageView.vue'), meta: { title: '用户管理', auth: true, admin: true } },
    { path: '/admin/audit', name: 'AdminAudit', component: () => import('../views/admin/ContentAuditView.vue'), meta: { title: '内容审核', auth: true, admin: true } },
    { path: '/admin/announcements', name: 'AdminAnnouncements', component: () => import('../views/admin/AnnouncementView.vue'), meta: { title: '公告管理', auth: true, admin: true } },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    // 路由切换时滚动到顶部
    scrollBehavior() {
        return { top: 0 }
    }
})

// 路由守卫 - 权限验证
router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = `${to.meta.title || '钓友圈'} - 钓友圈`
    const token = localStorage.getItem('token')
    // 需要登录但未登录
    if (to.meta.auth && !token) {
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
    }
    // 需要管理员权限
    if (to.meta.admin) {
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        if (user.role !== 'ADMIN') {
            next({ name: 'Home' })
            return
        }
    }
    next()
})

export default router
