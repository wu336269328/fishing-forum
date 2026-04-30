import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const globalCss = readFileSync(new URL('../src/styles/global.css', import.meta.url), 'utf8')
const homeView = readFileSync(new URL('../src/views/HomeView.vue', import.meta.url), 'utf8')
const postDetail = readFileSync(new URL('../src/views/PostDetailView.vue', import.meta.url), 'utf8')
const header = readFileSync(new URL('../src/components/layout/AppHeader.vue', import.meta.url), 'utf8')
const createPostView = readFileSync(new URL('../src/views/CreatePostView.vue', import.meta.url), 'utf8')
const messageView = readFileSync(new URL('../src/views/MessageView.vue', import.meta.url), 'utf8')
const spotMapView = readFileSync(new URL('../src/views/SpotMapView.vue', import.meta.url), 'utf8')

assert.match(globalCss, /--mobile-shell-max:\s*460px/)
assert.match(globalCss, /\.view-mobile \.main-content\s*\{[\s\S]*padding:\s*12px 14px calc\(96px \+ env\(safe-area-inset-bottom\)\)/)
assert.match(globalCss, /html,\s*body,\s*#app\s*\{[\s\S]*overflow-x:\s*hidden/)
assert.match(globalCss, /\.view-mobile \.mobile-tabbar\s*\{[\s\S]*bottom:\s*max\(10px, env\(safe-area-inset-bottom\)\)/)
assert.match(globalCss, /\.view-mobile \.mobile-tabbar\s*\{[\s\S]*left:\s*max\(10px, env\(safe-area-inset-left\)\)/)
assert.match(globalCss, /\.view-mobile \.mobile-tabbar\s*\{[\s\S]*right:\s*max\(10px, env\(safe-area-inset-right\)\)/)
assert.match(globalCss, /\.view-mobile \.mobile-tabbar \.create-tab\s*\{[\s\S]*transform:\s*translateY\(-6px\)/)
assert.match(header, /mobile-view-mode/)
assert.match(header, /desktop-view-switch/)
assert.match(header, /更多功能/)
assert.match(header, /视图模式/)

assert.match(homeView, /class="card post-item list-card fish-feed-card"/)
assert.match(homeView, /class="fish-pill section-pill"/)
assert.match(homeView, /class="post-hot-badge"/)
assert.match(homeView, /class="post-meta mobile-post-stats"/)

assert.match(postDetail, /class="card post-detail-card"/)
assert.match(postDetail, /class="action-bar post-action-bar"/)
assert.match(postDetail, /class="comment-card card"/)
assert.match(postDetail, /description="暂无评论，来抢沙发"/)

assert.match(header, /class="mobile-tabbar"/)
assert.match(header, /grid-template-columns:\s*repeat\(5, minmax\(0, 1fr\)\)/)
assert.match(createPostView, /class="form-actions"/)
assert.match(createPostView, /bottom:\s*calc\(96px \+ env\(safe-area-inset-bottom\)\)/)
assert.match(createPostView, /class="sub-form-title/)

assert.match(messageView, /class="message-layout"/)
assert.match(messageView, /chat-open/)
assert.match(messageView, /back-to-conversations/)

assert.match(spotMapView, /class="map-panel content-card"/)
assert.match(spotMapView, /width="min\(92vw, 620px\)"/)
