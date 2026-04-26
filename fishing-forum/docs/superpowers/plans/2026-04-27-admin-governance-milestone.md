# Admin Governance Milestone Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the first production-grade admin governance layer: user ban/mute, audit logs, sensitive words, report notes, and a stronger admin dashboard.

**Architecture:** Extend the existing Spring Boot monolith with lightweight entity/mapper additions and AdminService methods. Keep database changes in `schema.sql` for this project style, update tests first, and then wire the Vue admin pages to the new API fields/actions.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, JUnit/Mockito, Vue 3, Element Plus.

---

### Task 1: Backend Governance Model

**Files:**
- Modify: `backend/src/main/java/com/fishforum/entity/User.java`
- Modify: `backend/src/main/java/com/fishforum/entity/Report.java`
- Create: `backend/src/main/java/com/fishforum/entity/AdminLog.java`
- Create: `backend/src/main/java/com/fishforum/entity/SensitiveWord.java`
- Create: `backend/src/main/java/com/fishforum/mapper/AdminLogMapper.java`
- Create: `backend/src/main/java/com/fishforum/mapper/SensitiveWordMapper.java`
- Modify: `backend/src/main/resources/schema.sql`

- [ ] Add fields: user banned/muted state, report review note, admin log, sensitive word.
- [ ] Keep fields nullable/backward-compatible for existing databases.

### Task 2: Admin Service And Controller

**Files:**
- Modify: `backend/src/test/java/com/fishforum/service/AdminServiceTest.java`
- Modify: `backend/src/main/java/com/fishforum/service/AdminService.java`
- Modify: `backend/src/main/java/com/fishforum/controller/AdminController.java`
- Modify: `backend/src/main/java/com/fishforum/service/UserService.java`
- Modify: `backend/src/main/java/com/fishforum/service/PostService.java`
- Modify: `backend/src/main/java/com/fishforum/service/InteractionService.java`

- [ ] Write failing tests for role whitelist, self-protection, ban/mute, report notes, and logging.
- [ ] Implement service methods and controller endpoints.
- [ ] Block banned users from login and muted users from posting/commenting.

### Task 3: Admin Frontend

**Files:**
- Modify: `frontend/src/views/admin/DashboardView.vue`
- Modify: `frontend/src/views/admin/UserManageView.vue`
- Modify: `frontend/src/views/admin/ContentAuditView.vue`
- Modify: `frontend/src/views/admin/AnnouncementView.vue`

- [ ] Add dashboard cards and recent governance hints.
- [ ] Add ban/unban and mute/unmute controls.
- [ ] Add report detail note dialog and status actions.
- [ ] Keep UI in the current subdued blue-white theme.

### Task 4: Verification

- [ ] Run `mvn -Dmaven.repo.local=/tmp/m2 test`.
- [ ] Run `npm run build`.
- [ ] Review `git status --short` and leave unrelated outer files unstaged.
