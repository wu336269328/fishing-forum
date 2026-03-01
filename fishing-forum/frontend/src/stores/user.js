import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../api/request'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
    // 状态
    const token = ref(localStorage.getItem('token') || '')
    const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

    // 计算属性
    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => user.value?.role === 'ADMIN')
    const userId = computed(() => user.value?.id)

    // 登录
    async function login(username, password) {
        const res = await request.post('/api/auth/login', { username, password })
        if (res.code === 200) {
            token.value = res.data.token
            user.value = res.data.user
            localStorage.setItem('token', res.data.token)
            localStorage.setItem('user', JSON.stringify(res.data.user))
        }
        return res
    }

    // 注册
    async function register(username, password, email) {
        return await request.post('/api/auth/register', { username, password, email })
    }

    // 获取当前用户信息
    async function fetchCurrentUser() {
        const res = await request.get('/api/users/me')
        if (res.code === 200) {
            user.value = res.data
            localStorage.setItem('user', JSON.stringify(res.data))
        }
        return res
    }

    // 退出登录
    function logout() {
        token.value = ''
        user.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('user')
    }

    return { token, user, isLoggedIn, isAdmin, userId, login, register, fetchCurrentUser, logout }
})
