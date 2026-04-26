# Security And UI Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Harden the forum's security baseline and upgrade the core PC/mobile user experience.

**Architecture:** Backend changes keep the existing Spring Boot monolith but add explicit public endpoint whitelisting, environment-based secrets, upload validation, and lightweight rate limiting. Frontend changes add a global view-mode state, responsive layout primitives, a mobile navigation shell, and visual/interaction upgrades for the homepage, forum list, post detail, create post, profile, and admin dashboard.

**Tech Stack:** Spring Boot 3, Spring Security, MyBatis-Plus, Vue 3, Pinia, Element Plus, Vite, DOMPurify.

---

### Task 1: Backend Security Baseline

**Files:**
- Modify: `backend/src/main/resources/application.yml`
- Modify: `backend/src/main/java/com/fishforum/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/fishforum/config/RateLimitFilter.java`
- Create: `backend/src/main/java/com/fishforum/common/FileTypeValidator.java`
- Modify: `backend/src/main/java/com/fishforum/controller/UploadController.java`
- Modify: `backend/src/main/java/com/fishforum/controller/UserController.java`
- Test: `backend/src/test/java/com/fishforum/config/SecurityConfigTest.java`
- Test: `backend/src/test/java/com/fishforum/common/FileTypeValidatorTest.java`

- [ ] Add failing security tests proving private GET endpoints are not accidentally public and upload validation rejects unsafe files.
- [ ] Implement explicit public GET whitelists and leave all other API requests authenticated by default.
- [ ] Move database password and JWT secret to environment placeholders with safe local defaults.
- [ ] Add shared image validation for MIME, extension, file size, and magic bytes.
- [ ] Add a simple in-memory rate limit filter for auth, upload, comment, and report writes.
- [ ] Run `mvn -Dmaven.repo.local=/tmp/m2 test`.

### Task 2: Frontend View Mode And Layout Shell

**Files:**
- Create: `frontend/src/stores/viewMode.js`
- Modify: `frontend/src/App.vue`
- Modify: `frontend/src/components/layout/AppHeader.vue`
- Modify: `frontend/src/styles/global.css`

- [ ] Add global `auto | desktop | mobile` view mode with `localStorage` persistence.
- [ ] Apply root classes/data attributes for forced desktop and forced mobile modes.
- [ ] Add top view switcher and mobile menu.
- [ ] Add mobile bottom navigation with context-aware login/message/profile entry.
- [ ] Run `npm run build`.

### Task 3: Frontend Core Page Visual Refresh

**Files:**
- Modify: `frontend/src/views/HomeView.vue`
- Modify: `frontend/src/views/ForumView.vue`
- Modify: `frontend/src/views/PostDetailView.vue`
- Modify: `frontend/src/views/CreatePostView.vue`
- Modify: `frontend/src/views/ProfileView.vue`
- Modify: `frontend/src/views/admin/DashboardView.vue`
- Modify: `frontend/src/views/WikiDetailView.vue`

- [ ] Replace fragile inline-heavy layouts with page shells, hero blocks, responsive grids, and consistent cards.
- [ ] Improve mobile list density, filter layout, action bars, and empty/loading states.
- [ ] Sanitize wiki rendered HTML with DOMPurify.
- [ ] Keep existing API contracts unchanged.
- [ ] Run `npm run build`.

### Task 4: Final Verification

**Files:**
- Review all modified files.

- [ ] Run `mvn -Dmaven.repo.local=/tmp/m2 test`.
- [ ] Run `npm run build`.
- [ ] Run `git status --short` and ensure unrelated outer files remain unstaged.
