<template>
  <div class="app-container">
    <AppHeader />
    <main class="main-content"><router-view /></main>
    <footer class="footer">© 2025 钓友圈</footer>
  </div>
</template>

<script setup>
import AppHeader from './components/layout/AppHeader.vue'
</script>

<style scoped>
.app-container { display: flex; flex-direction: column; min-height: 100vh; }
.footer { text-align: center; padding: 16px; font-size: 12px; color: #999; border-top: 1px solid #eee; }
</style>
