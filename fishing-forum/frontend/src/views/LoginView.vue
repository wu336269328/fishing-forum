<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h2 class="auth-title">登录账号</h2>
      <p class="auth-subtitle">欢迎回来，开始今天的钓鱼分享。</p>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        autocomplete="on"
        size="large"
        class="auth-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="username" name="username" clearable @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password autocomplete="current-password" name="password" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" class="auth-submit" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
      <p class="auth-foot">还没有账号？<router-link to="/register">立即注册</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter(), route = useRoute(), userStore = useUserStore()
const loading = ref(false)
const formRef = ref(null)
const form = ref({ username: '', password: '' })

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 32, message: '用户名长度 2-32 字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    const res = await userStore.login(form.value.username, form.value.password)
    if (res.code === 200) {
      ElMessage.success('登录成功')
      router.push(route.query.redirect || '/')
    } else {
      ElMessage.error(res.message || '登录失败，请检查用户名和密码')
    }
  } catch (e) {
    ElMessage.error('登录失败，请稍后重试')
  }
  loading.value = false
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; padding: 60px 16px; }
.auth-card { width: 380px; max-width: 100%; padding: 28px; }
.auth-title { font-size: 22px; font-weight: 800; text-align: center; color: var(--ink); letter-spacing: -0.02em; }
.auth-subtitle { text-align: center; color: var(--muted); font-size: 13px; margin: 6px 0 18px; }
.auth-form { margin-top: 8px; }
.auth-submit { width: 100%; min-height: 44px; margin-top: 6px; border-radius: 12px !important; font-weight: 700; }
.auth-foot { text-align: center; margin-top: 14px; font-size: 13px; color: var(--muted); }
</style>
