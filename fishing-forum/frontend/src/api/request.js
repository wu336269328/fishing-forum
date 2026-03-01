import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * Axios请求实例 - 自动附加JWT令牌和错误处理
 */
const request = axios.create({
    baseURL: '',
    timeout: 15000,
})

// 请求拦截器 - 添加JWT令牌
request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// 响应拦截器 - 统一错误处理
request.interceptors.response.use(
    response => response.data,
    error => {
        if (error.response) {
            const { status } = error.response
            if (status === 401) {
                // 令牌过期，清除登录状态
                localStorage.removeItem('token')
                localStorage.removeItem('user')
                ElMessage.error('登录已过期，请重新登录')
                window.location.href = '/login'
            } else if (status === 403) {
                ElMessage.error('没有权限执行此操作')
            } else {
                ElMessage.error(error.response.data?.message || '请求失败')
            }
        } else {
            ElMessage.error('网络连接失败')
        }
        return Promise.reject(error)
    }
)

export default request
