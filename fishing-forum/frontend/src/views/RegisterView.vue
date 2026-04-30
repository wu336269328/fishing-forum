<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h2 class="auth-title">创建账号</h2>
      <p class="auth-subtitle">加入钓友圈，记录每一次出钓。</p>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        autocomplete="on"
        size="large"
        class="auth-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="2-32 字符，可使用中英文" autocomplete="username" name="username" clearable />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" type="email" placeholder="用于找回密码" autocomplete="email" name="email" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少 6 位" show-password autocomplete="new-password" name="new-password" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" show-password autocomplete="new-password" name="confirm-password" @keyup.enter="handleRegister" />
        </el-form-item>
        <el-button type="primary" class="auth-submit" :loading="loading" @click="handleRegister">注册</el-button>
      </el-form>
      <p class="auth-foot">已有账号？<router-link to="/login">直接登录</router-link></p>
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
const formRef = ref(null)
const form = ref({ username: '', email: '', password: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (!value) return callback(new Error('请再次输入密码'))
  if (value !== form.value.password) return callback(new Error('两次输入的密码不一致'))
  callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 32, message: '用户名长度 2-32 字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度 6-64 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirm, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    const res = await userStore.register(form.value.username, form.value.password, form.value.email)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '注册失败，请重试')
    }
  } catch (e) {
    ElMessage.error('注册失败，请稍后重试')
  }
  loading.value = false
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; padding: 40px 16px; }
.auth-card { width: 380px; max-width: 100%; padding: 28px; }
.auth-title { font-size: 22px; font-weight: 800; text-align: center; color: var(--ink); letter-spacing: -0.02em; }
.auth-subtitle { text-align: center; color: var(--muted); font-size: 13px; margin: 6px 0 18px; }
.auth-form { margin-top: 8px; }
.auth-submit { width: 100%; min-height: 44px; margin-top: 6px; border-radius: 12px !important; font-weight: 700; }
.auth-foot { text-align: center; margin-top: 14px; font-size: 13px; color: var(--muted); }
</style>
