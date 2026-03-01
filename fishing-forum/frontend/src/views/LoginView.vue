<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h2>登录</h2>
      <form @submit.prevent="handleLogin" style="margin-top:16px" autocomplete="on">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" autocomplete="username" name="username" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" show-password autocomplete="current-password" name="password" />
        </el-form-item>
        <el-button type="primary" style="width:100%" @click="handleLogin" :loading="loading">登录</el-button>
      </form>
      <p style="text-align:center; margin-top:12px; font-size:13px; color:#999">没有账号？<router-link to="/register">注册</router-link></p>
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
const form = ref({ username: '', password: '' })

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try {
    const res = await userStore.login(form.value.username, form.value.password)
    if (res.code === 200) { ElMessage.success('登录成功'); router.push(route.query.redirect || '/') }
    else ElMessage.error(res.message)
  } catch (e) { ElMessage.error('登录失败') }
  loading.value = false
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; padding-top: 80px; }
.auth-card { width: 360px; }
.auth-card h2 { font-size: 18px; text-align: center; }
</style>
