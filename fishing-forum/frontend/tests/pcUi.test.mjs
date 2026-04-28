import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const globalCss = readFileSync(new URL('../src/styles/global.css', import.meta.url), 'utf8')
const header = readFileSync(new URL('../src/components/layout/AppHeader.vue', import.meta.url), 'utf8')
const homeView = readFileSync(new URL('../src/views/HomeView.vue', import.meta.url), 'utf8')
const forumView = readFileSync(new URL('../src/views/ForumView.vue', import.meta.url), 'utf8')
const createPostView = readFileSync(new URL('../src/views/CreatePostView.vue', import.meta.url), 'utf8')
const dashboardView = readFileSync(new URL('../src/views/admin/DashboardView.vue', import.meta.url), 'utf8')

assert.match(globalCss, /\.view-auto \.main-content,\s*\.view-desktop \.main-content\s*\{[\s\S]*min-width:\s*0/)
assert.doesNotMatch(globalCss, /\.view-desktop \.main-content\s*\{[\s\S]*min-width:\s*1120px/)
assert.match(globalCss, /\.desktop-empty-card\s*\{[\s\S]*min-height:\s*180px/)

assert.match(header, /class="[^"]*logo-hook[^"]*"/)
assert.match(header, /class="[^"]*header-icon[^"]*"/)
assert.match(header, /class="user-avatar-fallback"/)

assert.match(homeView, /class="[^"]*desktop-empty-card[^"]*"/)
assert.match(homeView, /loadError/)
assert.match(homeView, /retryHomeData/)

assert.match(forumView, /class="forum-layout"/)
assert.match(forumView, /class="[^"]*desktop-empty-card[^"]*"/)
assert.match(forumView, /loadError/)

assert.match(createPostView, /class="create-workbench"/)
assert.match(createPostView, /class="[^"]*publish-aside[^"]*"/)

assert.match(dashboardView, /class="admin-dashboard-grid"/)
assert.match(dashboardView, /class="[^"]*admin-workbench-card[^"]*"/)
