<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h2>注册</h2>
      <form @submit.prevent="handleRegister" style="margin-top:16px" autocomplete="on">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" autocomplete="username" name="username" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.email" placeholder="邮箱" type="email" autocomplete="email" name="email" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码（6位以上）" show-password autocomplete="new-password" name="new-password" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" show-password autocomplete="new-password" name="confirm-password" />
        </el-form-item>
        <el-button type="primary" style="width:100%" @click="handleRegister" :loading="loading">注册</el-button>
      </form>
      <p style="text-align:center; margin-top:12px; font-size:13px; color:#999">已有账号？<router-link to="/login">登录</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter(), userStore = useUserStore()
const loading = ref(false)
const form = ref({ username: '', email: '', password: '', confirmPassword: '' })

const handleRegister = async () => {
  if (!form.value.username || !form.value.password) return ElMessage.warning('请填写完整')
  if (form.value.password !== form.value.confirmPassword) return ElMessage.warning('密码不一致')
  if (form.value.password.length < 6) return ElMessage.warning('密码至少6位')
  loading.value = true
  try {
    const res = await userStore.register(form.value.username, form.value.password, form.value.email)
    if (res.code === 200) { ElMessage.success('注册成功'); router.push('/login') }
    else ElMessage.error(res.message)
  } catch (e) { ElMessage.error('注册失败') }
  loading.value = false
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; padding-top: 60px; }
.auth-card { width: 360px; }
.auth-card h2 { font-size: 18px; text-align: center; }
</style>
