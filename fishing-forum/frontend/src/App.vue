<template>
  <div class="app-container" :class="viewMode.appClass" :data-view-mode="viewMode.mode">
    <AppHeader />
    <main class="main-content"><router-view /></main>
    <footer class="footer">© 2025 钓友圈 · 为认真钓鱼的人做的交流社区</footer>
  </div>
</template>

<script setup>
import AppHeader from './components/layout/AppHeader.vue'
import { useViewModeStore } from './stores/viewMode'

const viewMode = useViewModeStore()
</script>

<style scoped>
.app-container { display: flex; flex-direction: column; min-height: 100vh; }
.footer { text-align: center; padding: 18px 16px 88px; font-size: 12px; color: #7d7a70; border-top: 1px solid var(--line); background: rgba(255,255,255,0.72); }
</style>
