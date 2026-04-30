import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const globalCss = readFileSync(new URL('../src/styles/global.css', import.meta.url), 'utf8')
const header = readFileSync(new URL('../src/components/layout/AppHeader.vue', import.meta.url), 'utf8')
const homeView = readFileSync(new URL('../src/views/HomeView.vue', import.meta.url), 'utf8')
const forumView = readFileSync(new URL('../src/views/ForumView.vue', import.meta.url), 'utf8')
const spotMapView = readFileSync(new URL('../src/views/SpotMapView.vue', import.meta.url), 'utf8')
const messageView = readFileSync(new URL('../src/views/MessageView.vue', import.meta.url), 'utf8')
const userManageView = readFileSync(new URL('../src/views/admin/UserManageView.vue', import.meta.url), 'utf8')
const auditView = readFileSync(new URL('../src/views/admin/ContentAuditView.vue', import.meta.url), 'utf8')
const announcementView = readFileSync(new URL('../src/views/admin/AnnouncementView.vue', import.meta.url), 'utf8')

assert.match(globalCss, /\.view-auto \.main-content,\s*\.view-desktop \.main-content\s*\{[\s\S]*min-width:\s*0/)
assert.doesNotMatch(globalCss, /\.view-desktop \.main-content\s*\{[\s\S]*min-width:\s*1120px/)
assert.match(globalCss, /--color-primary:/)
assert.match(globalCss, /--surface-card:/)
assert.match(globalCss, /\.page-toolbar\s*\{/)
assert.match(globalCss, /\.ui-chip\s*\{/)
assert.match(globalCss, /\.admin-table-wrap\s*\{/)
assert.match(globalCss, /\.responsive-dialog \.el-dialog/)


assert.match(header, /class="[^"]*logo-hook[^"]*"/)
assert.match(header, /class="[^"]*header-icon[^"]*"/)
assert.match(header, /class="user-avatar-fallback"/)

assert.match(homeView, /class="[^"]*desktop-empty-card[^"]*"/)
assert.match(homeView, /loadError/)
assert.match(homeView, /retryHomeData/)

assert.match(forumView, /class="page-toolbar forum-filter-panel"/)
assert.match(forumView, /clearFilters/)
assert.match(forumView, /loadError/)

assert.match(spotMapView, /class="map-panel content-card"/)
assert.match(spotMapView, /class="map-pin"/)
assert.match(spotMapView, /rel="noopener noreferrer"/)

assert.match(messageView, /class="message-layout"/)
assert.match(messageView, /class="back-to-conversations"/)

assert.match(userManageView, /class="admin-table-wrap"/)
assert.match(userManageView, /ElMessageBox\.confirm/)
assert.match(auditView, /class="admin-table-wrap"/)
assert.match(auditView, /class="responsive-dialog"/)
assert.match(announcementView, /class="responsive-dialog"/)
assert.match(announcementView, /ElMessageBox\.confirm/)
