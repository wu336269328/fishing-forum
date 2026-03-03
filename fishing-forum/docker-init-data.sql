--
-- PostgreSQL database dump
--

\restrict OZLNXhMGf9AhbqSn5MEyu76thzuciUiiRcWaxphFF3897iQCjKTN5eKh0BHZeyV

-- Dumped from database version 16.11 (Ubuntu 16.11-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.11 (Ubuntu 16.11-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: announcements; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.announcements (id, title, content, is_active, created_at) VALUES (1, '欢迎来到钓鱼论坛！', '欢迎各位钓友加入我们的社区！在这里你可以分享渔获、交流技巧、推荐钓点。请遵守社区规则，文明交流。祝大家天天爆护！🎣', true, '2026-02-25 22:20:48.489823');
INSERT INTO public.announcements (id, title, content, is_active, created_at) VALUES (2, '社区规则公告', '为维护良好的社区氛围，请大家遵守以下规则：1. 文明用语，友善交流；2. 禁止发布广告和虚假信息；3. 尊重他人隐私，不随意转发他人信息；4. 举报违规内容，共建美好社区。', true, '2026-02-25 22:20:48.489823');


--
-- Data for Name: sections; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sections (id, name, description, icon, sort_order, post_count, created_at) VALUES (3, '装备讨论', '钓竿、鱼线、鱼饵等装备的讨论与推荐', '🎣', 3, 0, '2026-02-25 22:20:48.482719');
INSERT INTO public.sections (id, name, description, icon, sort_order, post_count, created_at) VALUES (4, '技巧攻略', '分享各种钓鱼技巧、方法和攻略', '📖', 4, 0, '2026-02-25 22:20:48.482719');
INSERT INTO public.sections (id, name, description, icon, sort_order, post_count, created_at) VALUES (5, '钓点推荐', '推荐好的钓鱼地点，交流钓场信息', '📍', 5, 0, '2026-02-25 22:20:48.482719');
INSERT INTO public.sections (id, name, description, icon, sort_order, post_count, created_at) VALUES (6, '赛事活动', '钓鱼比赛、线下活动信息发布', '🏆', 6, 0, '2026-02-25 22:20:48.482719');
INSERT INTO public.sections (id, name, description, icon, sort_order, post_count, created_at) VALUES (2, '渔获分享', '晒出你的战利品！分享每次出钓的收获', '🐟', 2, 1, '2026-02-25 22:20:48.482719');
INSERT INTO public.sections (id, name, description, icon, sort_order, post_count, created_at) VALUES (1, '综合交流', '钓鱼爱好者综合交流区，分享日常钓鱼趣事', '💬', 1, 2, '2026-02-25 22:20:48.482719');


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (13, 'testuser999', '$2a$10$r3nvINI.f/upm4nrOfgIBO8kI4X6NYJ0auyFgEXr4rKVTebnMJNbC', 'test999@test.com', '/default-avatar.png', '', 'USER', '2026-03-03 04:12:34.633764', '2026-03-03 04:12:34.633764', 0);
INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (14, 'testperm', '$2a$10$aXaQ8n0X936P9Dqr4/I10OQE0XXCG2hc4c0yexOqCfRZUi9L7jIU.', 'perm@t.com', '/default-avatar.png', '', 'USER', '2026-03-03 04:22:54.057436', '2026-03-03 04:22:54.057436', 0);
INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (1, 'admin', '$2a$10$bz6Z4nwrignCK7p59BenH.CbSbg/srqbt7B45l/GSjuBbdd.nsgYa', 'admin@fishforum.com', '/api/uploads/images/fish/鲫鱼_Carassius_auratus.jpg', '系统管理员', 'ADMIN', '2026-02-25 22:20:48.478609', '2026-02-25 22:20:48.478609', 0);
INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (2, '钓鱼达人', '$2a$10$bz6Z4nwrignCK7p59BenH.CbSbg/srqbt7B45l/GSjuBbdd.nsgYa', 'fisher@fishforum.com', '/api/uploads/images/fish/鲤鱼_Cyprinus_carpio.jpg', '热爱钓鱼的老玩家', 'USER', '2026-02-25 22:20:48.480898', '2026-02-25 22:20:48.480898', 0);
INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (3, '野钓小王', '$2a$10$bz6Z4nwrignCK7p59BenH.CbSbg/srqbt7B45l/GSjuBbdd.nsgYa', 'wang@fishforum.com', '/api/uploads/images/fish/草鱼_Ctenopharyngodon_idellus__油鲩.jpg', '专注野钓二十年', 'USER', '2026-02-25 22:20:48.481807', '2026-02-25 22:20:48.481807', 0);
INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (5, '路亚小哥', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8Co/a', 'luya@fishforum.com', '/api/uploads/images/fish/鲈鱼_Lateolabraxjaponicus.jpg', '路亚爱好者，主攻鲈鱼和翘嘴', 'USER', '2026-03-03 03:37:44.825818', '2026-03-03 03:37:44.825818', 0);
INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (6, '海钓老张', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8Co/a', 'zhang@fishforum.com', '/api/uploads/images/fish/青鱼_Mylopharyngodon_piceus.jpg', '海钓二十年，走遍中国海岸线', 'USER', '2026-03-03 03:37:44.825818', '2026-03-03 03:37:44.825818', 0);
INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (7, '台钓达人', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8Co/a', 'taidiao@fishforum.com', '/api/uploads/images/fish/鳊鱼_Parabramis_pekinensis.jpg', '竞技台钓选手，全国赛前十', 'USER', '2026-03-03 03:37:44.825818', '2026-03-03 03:37:44.825818', 0);
INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (8, '饵料研究员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8Co/a', 'erliao@fishforum.com', '/api/uploads/images/fish/鲶鱼_Silurus_asotus.jpg', '专注饵料搭配与研发十五年', 'USER', '2026-03-03 03:37:44.825818', '2026-03-03 03:37:44.825818', 0);
INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (9, '钓鱼小白', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8Co/a', 'xiaobai@fishforum.com', '/api/uploads/images/fish/鲢鱼_Hypophthalmichthys_molitrix.jpg', '刚入坑的新手，多多指教', 'USER', '2026-03-03 03:37:44.825818', '2026-03-03 03:37:44.825818', 0);
INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (10, '黑坑杀手', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8Co/a', 'heikeng@fishforum.com', '/api/uploads/images/fish/鳙鱼_Aristichthys_nobilis.jpg', '黑坑专家，偷驴冠军', 'USER', '2026-03-03 03:37:44.825818', '2026-03-03 03:37:44.825818', 0);
INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (11, '夜钓侠客', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8Co/a', 'yediao@fishforum.com', '/api/uploads/images/fish/黄颡鱼_Pelteobagrus_fulvidraco.jpg', '喜欢夜钓的感觉，安静又刺激', 'USER', '2026-03-03 03:37:44.825818', '2026-03-03 03:37:44.825818', 0);
INSERT INTO public.users (id, username, password, email, avatar, bio, role, created_at, updated_at, deleted) VALUES (12, '装备评测师', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8Co/a', 'ceping@fishforum.com', '/api/uploads/images/fish/鳡鱼_Elopichthys_bambusa.jpg', '专业渔具评测，公正客观', 'USER', '2026-03-03 03:37:44.825818', '2026-03-03 03:37:44.825818', 0);


--
-- Data for Name: posts; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (59, '自动化测试帖', '测试内容', 1, 1, false, false, 0, 0, 0, '2026-03-03 04:22:52.115855', '2026-03-03 04:22:52.115855', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (1, '今天野钓鲫鱼大丰收！', '<p>今天去水库野钓，连竿上鱼，总共钓了十几条鲫鱼，最大的有一斤多！用的4.5米的竿子，酒米打窝，红虫挂钩。天气不错，微风，水温大概12度左右。</p><p>分享一下今天的渔获，希望大家也能有好的收获！🎣</p>', 2, 2, false, false, 169, 23, 5, '2026-02-25 22:20:48.485148', '2026-02-25 22:20:48.485148', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (2, '新手入门：台钓装备选择指南', '<p>很多新手刚入坑不知道怎么选装备，我来分享一下个人经验：</p><h3>鱼竿选择</h3><p>新手建议从3.6米或4.5米的综合竿开始，调性选择28调，手感好且不容易断。品牌推荐光威、化氏等性价比高的国产品牌。</p><h3>线组搭配</h3><p>主线1.0+子线0.6，适合钓鲫鱼。如果目标鱼较大，可以适当加粗。</p><h3>浮漂选择</h3><p>巴尔杉木浮漂稳定性好，吃铅量1-2g适合静水作钓。</p>', 3, 3, false, false, 346, 45, 2, '2026-02-25 22:20:48.485148', '2026-02-25 22:20:48.485148', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (3, '分享一个绝佳野钓点位', '<p>最近发现一个很好的野钓点位，水质清澈，鱼种丰富，有鲫鱼、鲤鱼、草鱼等。周围环境也很好，适合周末家庭出行。</p><p>具体位置在城郊的一个小水库，交通方便，停车也不难。建议大家早去占位，周末人比较多。</p>', 2, 5, false, false, 104, 15, 2, '2026-02-25 22:20:48.485148', '2026-02-25 22:20:48.485148', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (60, '自动化测试帖', '测试内容', 1, 1, false, false, 1, 0, 0, '2026-03-03 07:12:54.127384', '2026-03-03 07:12:54.127384', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (4, '45454', '<p>5656</p><p><img src="/api/uploads/images/1977689d-b7b4-4c4a-bdbd-2ed10a77e6cf.jpg" style="max-width:100%"/></p>', 1, 2, false, false, 3, 0, 0, '2026-02-27 01:26:26.116453', '2026-02-27 01:26:26.116453', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (23, '周末水库狂拉鲫鱼30条！', '<p>周末去了城东水库，天气晴好，微风，水温适宜。用4.5米的竿子，酒米打窝，红虫挂钩。从早上6点一直到下午3点，连竿不断！</p><p>最大的鲫鱼有8两，平均半斤左右。这个季节鲫鱼真好钓，强烈推荐！🎣</p>', 5, 2, false, false, 289, 45, 2, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 7, 'CATCH');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (24, '钓到一条5斤大鲤鱼！', '<p>今天运气爆棚！在锦江河段钓到5斤大鲤鱼！螺鲤+九一八搭配商品饵，调四钓二。</p><p>中鱼的时候差点把竿子拉断，遛了20分钟才上岸。太激动了！</p>', 6, 2, false, false, 456, 67, 1, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 6, 'CATCH');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (25, '路亚翘嘴连竿分享', '<p>今天江边路亚，米诺7cm，抽了3个小时，连中8条翘嘴，最大3斤！</p><p>关键是找准标点，急流回旋处效果最好。慢收快抽，模仿受伤小鱼。</p>', 5, 2, false, false, 312, 38, 1, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 10, 'CATCH');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (26, '夜钓黄颡鱼大丰收', '<p>昨晚河边夜钓，蚯蚓做饵，晚8点到凌晨2点，钓了15条黄颡鱼。</p><p>窍门：选有石头水草的地方，小钩细线铅坠到底，荧光棒看漂！</p>', 11, 2, false, false, 178, 28, 0, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 21, 'CATCH');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (27, '光威鱼竿对比汉鼎——300元价位台钓竿横评', '<p>入手光威和汉鼎300元台钓竿，使用一个月做详细对比。</p><h3>手感</h3><p>光威偏硬适合快速回鱼；汉鼎偏软护线性好。</p><h3>做工</h3><p>光威涂装精致，汉鼎握把舒适。</p><h3>总结</h3><p>钓小鱼选汉鼎，钓大鱼选光威。</p>', 12, 3, false, false, 534, 56, 1, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 11, 'REVIEW');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (53, '钓鱼人的烦恼——老婆不让去钓鱼', '<p>后来带老婆一起去教她，没想到比我还上瘾，周末催着我去😂 解决问题就是让她也爱上钓鱼！</p>', 11, 1, false, false, 567, 78, 1, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (28, '天元蓝鲫+九一八——经典饵料搭配测评', '<p>老三样永远的神！测评蓝鲫和九一八搭配效果。</p><p>配方：蓝鲫40%+九一八40%+速攻20%。两小时上鱼12条，完胜纯蚯蚓对照组。</p>', 8, 3, false, false, 423, 48, 1, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 13, 'REVIEW');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (29, '佳钓尼浮漂评测——50元价位值不值？', '<p>巴尔杉木材质，做工精细目数清晰。吃铅量准确，翻身适中。</p><p>灵敏度够用，信号清晰，适合钓鲫鱼。性价比极高，新手首选！</p>', 12, 3, false, false, 267, 31, 0, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 14, 'REVIEW');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (30, '冬季野钓鲫鱼完全攻略', '<p>冬季是钓鲫鱼黄金季节。</p><h3>选位</h3><p>向阳避风深水区，水深2-3米。</p><h3>饵料</h3><p>红虫第一，蚯蚓次之。</p><h3>线组</h3><p>主线0.6-0.8，子线0.3-0.4。</p><h3>调漂</h3><p>调平水钓1-2目，抓小口。</p>', 6, 4, false, false, 678, 89, 1, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 7, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (31, '路亚入门：从零开始学路亚', '<p>路亚钓法近年很流行，更加主动和刺激。</p><h3>装备</h3><p>ML调路亚竿+2000型纺车轮+PE线1.0号。</p><h3>假饵</h3><p>米诺（浅水）、VIB（远投）、软虫（万能）。</p><h3>操竿</h3><p>匀速收线、抽停结合、底部跳跃。</p>', 5, 4, false, false, 892, 102, 1, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 2, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (32, '台钓调漂的终极指南', '<p>调漂是台钓最核心的技术。</p><h3>原理</h3><p>浮力=铅坠+饵料重力。</p><h3>常用调法</h3><p><b>调四钓二</b>：适合大部分情况。<br><b>调平水钓一目</b>：冬天灵敏度最高。</p>', 6, 4, false, false, 1023, 134, 0, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 1, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (33, '黑坑偷驴技巧大全', '<p>偷驴就是在放鱼后次日钓回锅鱼。</p><p>关键：选位（出鱼好的位置）、饵料（清淡为主）、钓法（糗鱼不频繁抛竿）、线组（放细1.0+0.6）。</p>', 10, 4, false, false, 345, 41, 0, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 5, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (34, '新手入坑，第一套装备怎么配？', '<p>各位钓友好，刚入坑新手，预算1000元，坐标广东，主钓鲫鱼和小鲤鱼。</p><p>鱼竿、鱼线、浮漂、钩子怎么选？有没有推荐的品牌？</p>', 9, 1, false, false, 234, 18, 1, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 1, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (35, '钓鱼人的烦恼——老婆不让去钓鱼怎么办？', '<p>估计很多钓友都有共鸣😂 后来我带老婆一起去，给她配了装备教她。</p><p>没想到她现在比我还上瘾，周末催着我去钓鱼😂 解决问题的最好办法就是让她也爱上钓鱼！</p>', 11, 1, false, false, 567, 78, 0, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (36, '分享DIY钓箱改装', '<p>普通钓箱花两天全面改装：加伞架+炮台座、自制饵料盘支架、LED灯条、加厚坐垫。</p><p>总花费不到200块，效果堪比1000+品牌钓箱！</p>', 10, 1, false, false, 412, 55, 0, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (37, '成都周边绝佳野钓点——龙泉湖', '<p>龙泉湖水质清澈鱼种丰富。推荐钓位：大坝左侧浅滩，水深1.5-2.5米。</p><p>交通：自驾约1小时。费用：免费（带走垃圾！）</p>', 6, 5, false, false, 456, 52, 1, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 4, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (38, '江苏太湖台钓攻略', '<p>太湖鱼类资源丰富。推荐春秋两季，目标鱼：鲫鲤鳊翘嘴。注意需办钓鱼证。</p>', 8, 5, false, false, 312, 38, 0, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, 4, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (39, '2026全国钓鱼锦标赛报名开始！', '<p>2026全国钓鱼锦标赛将在武汉举行。时间4.15-17，地点武汉东湖，报名费200元/人。</p>', 1, 6, false, false, 789, 65, 0, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (40, '周末约钓活动——杭州西湖旁', '<p>本周六约钓！杭州西湖旁小河，集合5:30，自带装备，有兴趣留言报名！</p>', 5, 6, false, false, 198, 24, 0, '2026-03-03 03:39:54.73231', '2026-03-03 03:39:54.73231', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (54, 'DIY钓箱改装分享', '<p>花两天改装：加伞架+炮台座+饵料盘+LED灯条+加厚坐垫，总花费不到200，堪比千元品牌钓箱！</p>', 10, 1, false, false, 412, 55, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (43, '路亚翘嘴连竿分享', '<p>江边路亚米诺7cm，3小时连中8条翘嘴最大3斤！关键找准标点，急流回旋处效果最好。</p>', 5, 2, false, false, 312, 38, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 10, 'CATCH');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (44, '夜钓黄颡鱼大丰收', '<p>昨晚河边夜钓蚯蚓做饵，晚8点到凌晨2点钓了15条黄颡鱼。选有石头水草的地方小钩细线到底。</p>', 11, 2, false, false, 178, 28, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 21, 'CATCH');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (45, '光威鱼竿对比汉鼎——300元台钓竿横评', '<p>入手光威和汉鼎300元台钓竿，一个月对比。</p><h3>手感</h3><p>光威偏硬回鱼快；汉鼎偏软护线好。</p><h3>做工</h3><p>光威涂装精致，汉鼎握把舒适。</p><p>总结：钓小鱼选汉鼎，钓大鱼选光威。</p>', 12, 3, false, false, 534, 56, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 11, 'REVIEW');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (47, '佳钓尼浮漂评测——50元值不值？', '<p>巴尔杉木材质做工精细目数清晰。吃铅量准确翻身适中。灵敏度够用，新手首选！</p>', 12, 3, false, false, 267, 31, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 14, 'REVIEW');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (48, '冬季野钓鲫鱼完全攻略', '<p>冬季钓鲫鱼黄金季节。</p><h3>选位</h3><p>向阳避风深水区2-3米。</p><h3>饵料</h3><p>红虫第一蚯蚓次之。</p><h3>线组</h3><p>主0.6-0.8子0.3-0.4。</p><h3>调漂</h3><p>调平水钓1-2目抓小口。</p>', 6, 4, false, false, 678, 89, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 7, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (49, '路亚入门：从零开始学路亚', '<p>路亚运动型钓法主动又刺激。</p><h3>装备</h3><p>ML调路亚竿+2000纺车轮+PE线1.0。</p><h3>假饵</h3><p>米诺VIB软虫。</p><h3>操竿</h3><p>匀速收线抽停结合底部跳跃。</p>', 5, 4, false, false, 892, 102, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 2, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (50, '台钓调漂终极指南', '<p>调漂是台钓最核心技术——让浮力=铅坠+饵料重力。</p><p><b>调四钓二</b>：适合大部分情况。<br><b>调平水钓一目</b>：冬天最灵敏。</p>', 6, 4, false, false, 1023, 134, 1, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 1, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (51, '黑坑偷驴技巧大全', '<p>偷驴就是放鱼后次日钓回锅鱼。选位出鱼好的，饵料清淡，糗鱼为主，线组放细1.0+0.6。</p>', 10, 4, false, false, 345, 41, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 5, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (52, '新手入坑第一套装备怎么配？', '<p>刚入坑新手预算1000元求推荐，坐标广东主钓鲫鱼小鲤鱼。鱼竿鱼线浮漂钩子怎么选？</p>', 9, 1, false, false, 234, 18, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 1, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (55, '成都周边绝佳野钓点龙泉湖', '<p>龙泉湖水质清澈鱼种丰富。推荐大坝左侧浅滩1.5-2.5米。自驾1小时免费开放。</p>', 6, 5, false, false, 456, 52, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 4, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (56, '江苏太湖台钓攻略', '<p>太湖资源丰富推荐春秋两季。目标鲫鲤鳊翘嘴。注意需办钓鱼证。</p>', 8, 5, false, false, 312, 38, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 4, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (57, '2026全国钓鱼锦标赛报名！', '<p>武汉东湖4.15-17，台钓个人赛团体赛，报名费200元/人。奖品丰厚！</p>', 1, 6, false, false, 789, 65, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (58, '周末约钓活动杭州西湖旁', '<p>本周六杭州西湖旁小河约钓！集合5:30东门停车场，自带装备饵料，有兴趣留言！</p>', 5, 6, false, false, 198, 24, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, NULL, 'NORMAL');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (41, '周末水库狂拉鲫鱼30条！', '<p>周末去了城东水库，天气晴好微风。4.5米竿子酒米打窝红虫挂钩，早6点到下午3点连竿不断！</p><p>最大鲫鱼8两，平均半斤。这个季节鲫鱼真好钓！🎣</p>', 5, 2, false, false, 290, 45, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 7, 'CATCH');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (46, '天元蓝鲫+九一八经典饵料搭配测评', '<p>老三样永远的神！蓝鲫40%+九一八40%+速攻20%，两小时上鱼12条，完胜纯蚯蚓对照组。</p>', 8, 3, false, false, 424, 48, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 13, 'REVIEW');
INSERT INTO public.posts (id, title, content, user_id, section_id, is_top, is_featured, view_count, like_count, comment_count, created_at, updated_at, deleted, tag_id, post_type) VALUES (42, '钓到一条5斤大鲤鱼！', '<p>运气爆棚！锦江河段钓到5斤大鲤鱼！螺鲤+九一八搭配，调四钓二。遛了20分钟才上岸！</p>', 6, 2, false, false, 457, 67, 0, '2026-03-03 03:45:01.185667', '2026-03-03 03:45:01.185667', 0, 6, 'CATCH');


--
-- Data for Name: catch_records; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.catch_records (id, post_id, fish_species, weight, length, bait, spot_name, weather, photo_url, fishing_date, created_at) VALUES (2, 23, '鲫鱼', 0.5, NULL, '红虫', '城东水库', '晴 微风', NULL, '2026-02-22', '2026-03-03 03:45:01.196023');
INSERT INTO public.catch_records (id, post_id, fish_species, weight, length, bait, spot_name, weather, photo_url, fishing_date, created_at) VALUES (3, 41, '鲫鱼', 0.5, NULL, '红虫', '城东水库', '晴 微风', NULL, '2026-02-22', '2026-03-03 03:45:01.196023');
INSERT INTO public.catch_records (id, post_id, fish_species, weight, length, bait, spot_name, weather, photo_url, fishing_date, created_at) VALUES (4, 24, '鲤鱼', 5, NULL, '螺鲤+九一八', '锦江河段', '多云 无风', NULL, '2026-02-25', '2026-03-03 03:45:01.199065');
INSERT INTO public.catch_records (id, post_id, fish_species, weight, length, bait, spot_name, weather, photo_url, fishing_date, created_at) VALUES (5, 42, '鲤鱼', 5, NULL, '螺鲤+九一八', '锦江河段', '多云 无风', NULL, '2026-02-25', '2026-03-03 03:45:01.199065');
INSERT INTO public.catch_records (id, post_id, fish_species, weight, length, bait, spot_name, weather, photo_url, fishing_date, created_at) VALUES (6, 25, '翘嘴', 3, NULL, '米诺7cm', '长江江段', '晴 微风', NULL, '2026-02-20', '2026-03-03 03:45:01.20014');
INSERT INTO public.catch_records (id, post_id, fish_species, weight, length, bait, spot_name, weather, photo_url, fishing_date, created_at) VALUES (7, 43, '翘嘴', 3, NULL, '米诺7cm', '长江江段', '晴 微风', NULL, '2026-02-20', '2026-03-03 03:45:01.20014');
INSERT INTO public.catch_records (id, post_id, fish_species, weight, length, bait, spot_name, weather, photo_url, fishing_date, created_at) VALUES (8, 26, '黄颡鱼', 0.3, NULL, '蚯蚓', '城南小河', '晴 无风', NULL, '2026-02-18', '2026-03-03 03:45:01.201248');
INSERT INTO public.catch_records (id, post_id, fish_species, weight, length, bait, spot_name, weather, photo_url, fishing_date, created_at) VALUES (9, 44, '黄颡鱼', 0.3, NULL, '蚯蚓', '城南小河', '晴 无风', NULL, '2026-02-18', '2026-03-03 03:45:01.201248');


--
-- Data for Name: comments; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (1, '太厉害了！请问用的什么饵料？', 1, 3, NULL, 0, '2026-02-25 22:20:48.486296', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (2, '红虫效果确实好，冬天必备', 1, 2, 1, 0, '2026-02-25 22:20:48.486296', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (3, '收藏了！新手必读', 2, 2, NULL, 0, '2026-02-25 22:20:48.486296', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (4, '写得很详细，感谢分享', 2, 3, NULL, 0, '2026-02-25 22:20:48.486296', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (5, '请问具体在哪个位置呀？', 3, 3, NULL, 0, '2026-02-25 22:20:48.486296', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (6, '666', 1, 1, NULL, 0, '2026-02-25 22:28:29.254861', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (7, 'iuyiu', 3, 1, NULL, 0, '2026-02-27 02:07:44.682513', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (41, '30条鲫鱼太厉害了，我上次半天才3条😂', 23, 7, NULL, 0, '2026-03-03 03:45:01.206363', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (42, '请问是哪个水库？', 23, 9, NULL, 0, '2026-03-03 03:45:01.206363', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (43, '5斤鲤鱼真不错！遛鱼技术到位', 24, 5, NULL, 0, '2026-03-03 03:45:01.206363', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (44, '路亚翘嘴确实爽一竿中鱼太棒了', 25, 11, NULL, 0, '2026-03-03 03:45:01.206363', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (45, '光威确实不错用了3年没坏', 27, 5, NULL, 0, '2026-03-03 03:45:01.206363', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (46, '老三样永远的神我也一直用', 28, 7, NULL, 0, '2026-03-03 03:45:01.206363', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (47, '冬天钓鲫鱼心得总结很全面收藏', 30, 9, NULL, 0, '2026-03-03 03:45:01.206363', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (48, '终于搞懂调漂了写得太好', 50, 9, NULL, 0, '2026-03-03 03:45:01.206363', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (49, '哈哈我老婆也被带入坑了😄', 53, 8, NULL, 0, '2026-03-03 03:45:01.206363', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (50, '新手建议买光威竿子性价比高', 34, 6, NULL, 0, '2026-03-03 03:45:01.206363', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (51, '龙泉湖确实不错上个月去过', 37, 10, NULL, 0, '2026-03-03 03:45:01.206363', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (52, '路亚入门写得很详细新手必读', 31, 12, NULL, 0, '2026-03-03 03:45:01.206363', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (53, '测试评论', 1, 1, NULL, 0, '2026-03-03 04:22:52.260106', 0);
INSERT INTO public.comments (id, content, post_id, user_id, parent_id, like_count, created_at, deleted) VALUES (54, '测试评论', 1, 1, NULL, 0, '2026-03-03 07:12:54.270844', 0);


--
-- Data for Name: favorites; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: fishing_spots; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (2, '锦江河段', '市区内的一段河流钓场，交通便利。主要鱼种为鲫鱼和鲤鱼，偶尔能钓到翘嘴。', 30.5726, 104.0665, '鲫鱼,鲤鱼,翘嘴', '河流', '全天开放', 3.8, 0, 3, '2026-02-25 22:20:48.487562', 0, NULL);
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (3, '青龙湖', '大型湖泊，适合船钓和岸钓。鱼种众多，秋季鲈鱼活跃。需要购买钓鱼证。', 30.573, 104.067, '鲈鱼,鲤鱼,鲫鱼,鳜鱼', '湖泊', '06:00-20:00', 4.2, 0, 2, '2026-02-25 22:20:48.487562', 0, NULL);
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (1, '翠湖水库', '环境优美的中型水库，水质清澈，鱼种丰富。适合台钓和路亚。周边有停车场和简易卫生间。', 30.5728, 104.0668, '鲫鱼,鲤鱼,草鱼,鲢鳙', '水库', '全天开放', 5, 1, 2, '2026-02-25 22:20:48.487562', 0, NULL);
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (4, '1', '', 31.0305, 112.2506, '', '水库', '全天开放', 0, 0, 1, '2026-02-27 15:33:56.020034', 0, '');
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (15, '千岛湖', '浙江著名湖泊钓场水质极佳鱼种丰富。大鱼多适合船钓和岸钓。', 29.6048, 119.0019, '鲫鱼,鲤鱼,草鱼,鲢鳙,翘嘴', '湖泊', '06:00-18:00', 4.8, 0, 5, '2026-03-03 03:45:01.190175', 0, NULL);
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (16, '太湖西山', '太湖西山水域宽广秋季鲈鱼路亚效果极佳。', 31.071, 120.241, '鲫鱼,鲤鱼,鲈鱼,翘嘴,鳊鱼', '湖泊', '全天开放', 4.5, 0, 8, '2026-03-03 03:45:01.190175', 0, NULL);
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (17, '洞庭湖岳阳段', '洞庭湖鱼类资源丰富野钓天堂。大鲤鱼草鱼鲢鳙众多。', 29.357, 113.094, '鲤鱼,草鱼,鲢鳙,鳊鱼,青鱼', '湖泊', '全天开放', 4.6, 0, 6, '2026-03-03 03:45:01.190175', 0, NULL);
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (18, '密云水库', '北京人气最高野钓水域交通便利需提前预约钓位。', 40.5, 116.85, '鲫鱼,鲤鱼,草鱼,鲢鳙', '水库', '05:00-19:00', 4.3, 0, 10, '2026-03-03 03:45:01.190175', 0, NULL);
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (19, '崇明岛东滩', '上海崇明岛海钓矶钓钓点。主要鲈鱼黑鲷。', 31.52, 121.92, '鲈鱼,黑鲷,黄鱼', '海钓', '全天开放', 4.1, 0, 6, '2026-03-03 03:45:01.190175', 0, NULL);
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (20, '松花江哈尔滨段', '松花江水域广阔冬季冰钓特色夏季抛竿远投。', 45.75, 126.65, '鲤鱼,鲫鱼,鲶鱼,鳡鱼', '河流', '全天开放', 4.4, 0, 11, '2026-03-03 03:45:01.190175', 0, NULL);
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (21, '钱塘江富阳段', '杭州优质野钓点翘嘴鲈鱼丰富路亚效果好。', 30.05, 119.96, '翘嘴,鲈鱼,鲤鱼,鲫鱼', '河流', '全天开放', 4.2, 0, 5, '2026-03-03 03:45:01.190175', 0, NULL);
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (22, '丹江口水库', '南水北调水源地水质极佳大物频出。', 32.54, 111.51, '鲤鱼,草鱼,鲢鳙,翘嘴,青鱼', '水库', '06:00-18:00', 4.7, 0, 6, '2026-03-03 03:45:01.190175', 0, NULL);
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (23, '白洋淀', '华北最大淡水湿地以鲫鱼黑鱼为主。', 38.93, 115.92, '鲫鱼,黑鱼,鲤鱼,黄颡鱼', '湖泊', '全天开放', 4, 0, 10, '2026-03-03 03:45:01.190175', 0, NULL);
INSERT INTO public.fishing_spots (id, name, description, latitude, longitude, fish_types, spot_type, open_time, rating, review_count, user_id, created_at, deleted, image_url) VALUES (24, '广州流溪河', '广东知名野钓河全年可钓冬季水温适合鲫鱼。', 23.485, 113.283, '鲫鱼,鲤鱼,草鱼,罗非鱼', '河流', '全天开放', 3.9, 0, 7, '2026-03-03 03:45:01.190175', 0, NULL);


--
-- Data for Name: follows; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: gear_reviews; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.gear_reviews (id, post_id, brand, model, gear_category, price, rating, pros, cons, photo_url, created_at) VALUES (2, 27, '光威/汉鼎', '300元价位对比', '鱼竿', 300, 4, '性价比高做工精细', '竿稍稍粗糙', NULL, '2026-03-03 03:45:01.202197');
INSERT INTO public.gear_reviews (id, post_id, brand, model, gear_category, price, rating, pros, cons, photo_url, created_at) VALUES (3, 45, '光威/汉鼎', '300元价位对比', '鱼竿', 300, 4, '性价比高做工精细', '竿稍稍粗糙', NULL, '2026-03-03 03:45:01.202197');
INSERT INTO public.gear_reviews (id, post_id, brand, model, gear_category, price, rating, pros, cons, photo_url, created_at) VALUES (4, 28, '天元', '蓝鲫+九一八', '饵料', 45, 5, '经典配方适用范围广', '保质期短', NULL, '2026-03-03 03:45:01.20391');
INSERT INTO public.gear_reviews (id, post_id, brand, model, gear_category, price, rating, pros, cons, photo_url, created_at) VALUES (5, 46, '天元', '蓝鲫+九一八', '饵料', 45, 5, '经典配方适用范围广', '保质期短', NULL, '2026-03-03 03:45:01.20391');
INSERT INTO public.gear_reviews (id, post_id, brand, model, gear_category, price, rating, pros, cons, photo_url, created_at) VALUES (6, 29, '佳钓尼', '巴尔杉木浮漂', '浮漂', 50, 4, '灵敏度高目数清晰', '吃铅量偏小', NULL, '2026-03-03 03:45:01.205119');
INSERT INTO public.gear_reviews (id, post_id, brand, model, gear_category, price, rating, pros, cons, photo_url, created_at) VALUES (7, 47, '佳钓尼', '巴尔杉木浮漂', '浮漂', 50, 4, '灵敏度高目数清晰', '吃铅量偏小', NULL, '2026-03-03 03:45:01.205119');


--
-- Data for Name: likes; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.notifications (id, user_id, type, title, content, related_id, is_read, created_at) VALUES (1, 2, 'FOLLOW', '新增关注', '有人关注了你', 1, false, '2026-03-03 04:22:53.690235');


--
-- Data for Name: tags; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.tags (id, name, section_id, color) VALUES (1, '台钓', 1, '#0ea5e9');
INSERT INTO public.tags (id, name, section_id, color) VALUES (2, '路亚', 1, '#8b5cf6');
INSERT INTO public.tags (id, name, section_id, color) VALUES (3, '海钓', 1, '#f59e0b');
INSERT INTO public.tags (id, name, section_id, color) VALUES (4, '野钓', 1, '#10b981');
INSERT INTO public.tags (id, name, section_id, color) VALUES (5, '黑坑', 1, '#ef4444');
INSERT INTO public.tags (id, name, section_id, color) VALUES (6, '鲤鱼', 2, '#0ea5e9');
INSERT INTO public.tags (id, name, section_id, color) VALUES (7, '鲫鱼', 2, '#10b981');
INSERT INTO public.tags (id, name, section_id, color) VALUES (8, '鲈鱼', 2, '#8b5cf6');
INSERT INTO public.tags (id, name, section_id, color) VALUES (9, '鳜鱼', 2, '#f59e0b');
INSERT INTO public.tags (id, name, section_id, color) VALUES (10, '翘嘴', 2, '#ec4899');
INSERT INTO public.tags (id, name, section_id, color) VALUES (11, '鱼竿', 3, '#0ea5e9');
INSERT INTO public.tags (id, name, section_id, color) VALUES (12, '鱼线', 3, '#10b981');
INSERT INTO public.tags (id, name, section_id, color) VALUES (13, '饵料', 3, '#f59e0b');
INSERT INTO public.tags (id, name, section_id, color) VALUES (14, '浮漂', 3, '#8b5cf6');
INSERT INTO public.tags (id, name, section_id, color) VALUES (15, '鱼钩', 3, '#ef4444');
INSERT INTO public.tags (id, name, section_id, color) VALUES (16, '草鱼', NULL, '#22c55e');
INSERT INTO public.tags (id, name, section_id, color) VALUES (17, '鲢鳙', NULL, '#06b6d4');
INSERT INTO public.tags (id, name, section_id, color) VALUES (18, '黑鱼', NULL, '#6366f1');
INSERT INTO public.tags (id, name, section_id, color) VALUES (19, '青鱼', NULL, '#14b8a6');
INSERT INTO public.tags (id, name, section_id, color) VALUES (20, '鲶鱼', NULL, '#f97316');
INSERT INTO public.tags (id, name, section_id, color) VALUES (21, '黄颡鱼', NULL, '#eab308');


--
-- Data for Name: post_tags; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: reports; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: spot_reviews; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.spot_reviews (id, spot_id, user_id, rating, content, created_at) VALUES (1, 1, 1, 5, '11', '2026-02-27 00:40:45.842841');


--
-- Data for Name: wiki_entries; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (1, '鲫鱼', '# 鲫鱼\n\n鲫鱼（学名：Carassius auratus）是淡水鱼中最常见的鱼种之一，也是钓鱼爱好者最常钓获的鱼种。\n\n## 生活习性\n- 栖息于水体底层\n- 杂食性，偏爱植物性饵料\n- 耐低温，冬季仍可垂钓\n\n## 钓鱼技巧\n- **饵料选择**：春夏用商品饵（腥香型），秋冬用红虫、蚯蚓\n- **钓组搭配**：主线0.8-1.2号，子线0.4-0.6号\n- **调漂方法**：调四钓二适合大多数情况', '鱼种', 2, 1, 6, '2026-02-25 22:20:48.48863', '2026-02-25 22:20:48.48863', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (2, '蚯蚓饵', '# 蚯蚓饵\n\n蚯蚓是最经典的钓鱼饵料之一，被称为"万能饵"。\n\n## 适用范围\n适合钓鲫鱼、鲤鱼、黄颡鱼、鲶鱼等多种鱼类。\n\n## 使用方法\n1. 选择红色蚯蚓，活力好的效果更佳\n2. 穿钩时不要露出钩尖太多\n3. 可以蘸一些商品饵粉增加诱鱼效果', '饵料', 3, 1, 4, '2026-02-25 22:20:48.48863', '2026-02-25 22:20:48.48863', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (3, '台钓基础', '# 台钓基础\n\n台钓（悬坠钓法）是目前最流行的淡水钓法之一。\n\n## 基本装备\n- 台钓竿（3.6-6.3米）\n- 主线、子线\n- 浮漂、铅坠\n- 钓台或钓箱\n\n## 调漂方法\n1. 空钩半水调漂\n2. 确定调目（通常调4目）\n3. 挂饵找底\n4. 确定钓目（通常钓2目）
![图片](/api/uploads/images/f73f7f3b-7608-4d74-9705-3c87a4848c3a.jpg)
', '技巧', 2, 2, 6, '2026-02-25 22:20:48.48863', '2026-02-25 22:20:48.48863', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (24, '鲤鱼', '# 鲤鱼（Cyprinus carpio）

![鲤鱼](/api/uploads/images/fish/鲤鱼_Cyprinus_carpio.jpg)

## 基本信息
- **学名**：Cyprinus carpio
- **别名**：鲤拐子、毛子
- **体长**：30-60cm
- **分布**：全国各大淡水水域

## 生活习性
底栖鱼类喜在水底觅食。杂食性喜食螺蛳蚯蚓水草嫩芽。力量大冲击力强。

## 垂钓技巧
- **最佳季节**：春季产卵前后
- **饵料**：螺鲤、荒食、玉米粒
- **钓组**：主线2.0-3.0，子线1.0-1.5
- **关键**：打重窝聚鱼耐心守钓', '鱼种图鉴', 3, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (39, '毛鳞鱼', '# 毛鳞鱼（Osmerus mordax）

![毛鳞鱼](/api/uploads/images/fish/毛鳞鱼Osmerus_mordax.jpg)

## 基本信息
- **学名**：Osmerus mordax
- **别名**：多春鱼、胡瓜鱼

## 生活习性
冷水性小型鱼类多在近海河口活动。因鱼卵丰富得名多春鱼，日料常见食材。', '鱼种图鉴', 6, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (23, '鲫鱼', '# 鲫鱼（Carassius auratus）

![鲫鱼](/api/uploads/images/fish/鲫鱼_Carassius_auratus.jpg)

## 基本信息
- **学名**：Carassius auratus
- **别名**：河鲫鱼、月鲫仔、土鲫
- **体长**：通常15-25cm
- **分布**：全国各地淡水水域

## 生活习性
栖息于水体底层和中层，杂食性，偏爱植物性饵料和小型水生动物。耐低温，冬季仍可正常进食，是四季可钓的鱼种。

## 垂钓技巧
- **最佳季节**：春秋两季，冬季也可
- **饵料选择**：春夏商品饵腥香型，秋冬红虫蚯蚓
- **钓组搭配**：主线0.8-1.2，子线0.4-0.6
- **调漂**：调四钓二，草边坎下回水湾最佳', '鱼种图鉴', 2, 1, 3, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (25, '草鱼', '# 草鱼（Ctenopharyngodon idellus）

![草鱼](/api/uploads/images/fish/草鱼_Ctenopharyngodon_idellus__油鲩.jpg)

## 基本信息
- **学名**：Ctenopharyngodon idellus
- **别名**：油鲩、草棒
- **体长**：可达60-100cm

## 生活习性
中下层鱼类草食性为主。喜食嫩草玉米叶。生长迅速体型大夏季最活跃。

## 垂钓技巧
- **最佳季节**：夏季6-9月
- **饵料**：嫩草、玉米粒、芦苇芯
- **钓法**：浮钓或离底钓', '鱼种图鉴', 2, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (26, '青鱼', '# 青鱼（Mylopharyngodon piceus）

![青鱼](/api/uploads/images/fish/青鱼_Mylopharyngodon_piceus.jpg)

## 基本信息
- **学名**：Mylopharyngodon piceus
- **别名**：黑鲩、螺蛳青
- **体长**：可达100cm以上

## 生活习性
底层鱼类以螺蛳蚌壳等软体动物为食。咽齿发达体型巨大。

## 垂钓技巧
- **饵料**：螺蛳、蚌肉、玉米粒
- **钓组**：主线4.0以上子线2.0以上
- **关键**：重铅守底大钩大线', '鱼种图鉴', 3, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (27, '鲈鱼', '# 鲈鱼（Lateolabrax japonicus）

![鲈鱼](/api/uploads/images/fish/鲈鱼_Lateolabraxjaponicus.jpg)

## 基本信息
- **学名**：Lateolabrax japonicus
- **别名**：花鲈、七星鲈
- **体长**：30-50cm

## 生活习性
肉食性以小鱼虾为食。活跃于中上层喜欢有流水环境。路亚理想目标鱼。

## 垂钓技巧
- **最佳季节**：秋季9-11月
- **饵料**：活虾小鱼路亚假饵
- **关键**：桥墩防波堤附近效果最好', '鱼种图鉴', 5, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (28, '鲢鱼', '# 鲢鱼（Hypophthalmichthys molitrix）

![鲢鱼](/api/uploads/images/fish/鲢鱼_Hypophthalmichthys_molitrix.jpg)

## 基本信息
- **学名**：Hypophthalmichthys molitrix
- **别名**：白鲢、跳鲢

## 生活习性
中上层滤食性以浮游生物为食。喜温怕冷夏季最活跃。

## 垂钓技巧
- **饵料**：酸臭饵、发酵饵
- **钓法**：浮钓1-2米水深
- **关键**：大饵快抛雾化要好', '鱼种图鉴', 2, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (29, '鳙鱼', '# 鳙鱼（Aristichthys nobilis）

![鳙鱼](/api/uploads/images/fish/鳙鱼_Aristichthys_nobilis.jpg)

## 基本信息
- **学名**：Aristichthys nobilis
- **别名**：花鲢、胖头鱼、大头鱼

## 生活习性
中上层鱼类头部宽大以浮游动物为主食。

## 垂钓技巧
- **饵料**：酸臭发酵饵蒜味饵
- **钓法**：浮钓1.5-3米
- **关键**：大线大钩', '鱼种图鉴', 3, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (31, '鳊鱼', '# 鳊鱼（Parabramis pekinensis）

![鳊鱼](/api/uploads/images/fish/鳊鱼_Parabramis_pekinensis.jpg)

## 基本信息
- **学名**：Parabramis pekinensis
- **别名**：武昌鱼、团头鲂

## 生活习性
中下层杂食偏草食。体扁味美春夏活跃。

## 垂钓技巧
- **饵料**：蚯蚓红虫商品饵
- **钓法**：台钓离底或钓浮
- **关键**：鱼口轻细线小钩', '鱼种图鉴', 2, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (32, '黄颡鱼', '# 黄颡鱼（Pelteobagrus fulvidraco）

![黄颡鱼](/api/uploads/images/fish/黄颡鱼_Pelteobagrus_fulvidraco.jpg)

## 基本信息
- **学名**：Pelteobagrus fulvidraco
- **别名**：黄辣丁、黄骨鱼、昂刺鱼

## 生活习性
底栖夜行肉食性。背鳍胸鳍有硬刺需小心。味道鲜美。

## 垂钓技巧
- **饵料**：蚯蚓首选
- **钓法**：夜钓底钓
- **关键**：铅坠到底荧光棒看漂', '鱼种图鉴', 11, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (33, '鳝鱼', '# 鳝鱼（Monopterus albus）

![鳝鱼](/api/uploads/images/fish/鳝鱼_Monopterus_albus.jpg)

## 基本信息
- **学名**：Monopterus albus
- **别名**：黄鳝、长鱼

## 生活习性
穴居鱼类栖息于水田浅水沟渠泥洞中。夜间出洞觅食。

## 垂钓技巧
- **钓法**：钓鳝钩探洞
- **饵料**：蚯蚓', '鱼种图鉴', 3, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (40, '叶子鱼', '# 叶子鱼

![叶子鱼](/api/uploads/images/fish/叶子鱼.jpg)

## 基本信息
- **别名**：枯叶鱼

## 生活习性
小型淡水鱼体型扁平似枯叶。具有极强拟态能力，是自然界拟态的绝佳范例。', '鱼种图鉴', 2, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (37, '鲳鱼', '# 鲳鱼

![鲳鱼](/api/uploads/images/fish/鲳鱼.jpg)

## 基本信息
- **别名**：平鱼、镜鱼
- **分布**：中国沿海水域

## 生活习性
海洋中上层银白色扁平似镜。肉质鲜嫩优质海鱼。

## 垂钓
- **钓法**：矶钓船钓
- **饵料**：虾肉小鱼', '鱼种图鉴', 6, 1, 4, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (38, '马脑鱼', '# 马脑鱼（Managatsuo）

![马脑鱼](/api/uploads/images/fish/马脑鱼(Managatsuo).jpg)

## 基本信息
- **别名**：刺鲳、马鲛
- **分布**：中国沿海

## 生活习性
海洋中层体型较大肉质肥美。

## 垂钓
- **钓法**：船钓拖钓
- **饵料**：活虾鱿鱼条', '鱼种图鉴', 6, 1, 1, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (34, '鳡鱼', '# 鳡鱼（Elopichthys bambusa）

![鳡鱼](/api/uploads/images/fish/鳡鱼_Elopichthys_bambusa.jpg)

## 基本信息
- **学名**：Elopichthys bambusa
- **别名**：水老虎、竿鱼

## 生活习性
大型肉食性淡水霸王。游速极快以其他鱼类为食。路亚终极目标。

## 垂钓技巧
- **饵料**：大型路亚假饵
- **关键**：粗线大钩准备长时间溜鱼', '鱼种图鉴', 5, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (35, '泥鳅', '# 泥鳅（Misgurnus anguillicaudatus）

![泥鳅](/api/uploads/images/fish/泥鳅_Misgurnus_anguillicaudatus_.jpg)

## 基本信息
- **学名**：Misgurnus anguillicaudatus
- **别名**：鳅鱼

## 生活习性
底栖性喜钻入泥中杂食性。耐低氧生命力顽强。

## 垂钓用途
主要作为活饵使用，钓鲈鱼翘嘴鳜鱼的绝佳饵料。', '鱼种图鉴', 2, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (36, '鲥鱼', '# 鲥鱼（Tenualosa reevesii）

![鲥鱼](/api/uploads/images/fish/鲥鱼_Tenualosa_reevesii_Hilsa_reevesi.jpg)

## 基本信息
- **学名**：Tenualosa reevesii
- **别名**：三来鱼、时鱼
- 长江三鲜之一

## ⚠️ 保护状态
鲥鱼已被列为国家重点保护水生野生动物，**严禁捕捞**！本词条仅供科普。', '鱼种图鉴', 2, 1, 0, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (41, '虱目鱼', '# 虱目鱼（Milkfish）

![虱目鱼](/api/uploads/images/fish/Milkfish.jpg)

## 基本信息
- **学名**：Chanos chanos
- **别名**：遮目鱼、牛奶鱼

## 生活习性
温水性以藻类浮游生物为食。广泛养殖于东南亚和台湾。肉质细腻鲜美。

## 垂钓
- **钓法**：浮钓或底钓
- **饵料**：面团藻类饵', '鱼种图鉴', 6, 1, 1, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);
INSERT INTO public.wiki_entries (id, title, content, category, user_id, version, view_count, created_at, updated_at, deleted) VALUES (30, '鲶鱼', '# 鲶鱼（Silurus asotus）

![鲶鱼](/api/uploads/images/fish/鲶鱼_Silurus_asotus.jpg)

## 基本信息
- **学名**：Silurus asotus
- **别名**：鲇鱼、胡子鲶

## 生活习性
夜行性底层肉食性鱼类。白天躲石缝洞穴夜间觅食。

## 垂钓技巧
- **最佳时间**：夏秋夜间
- **饵料**：蚯蚓、鸡肝、小鱼
- **钓法**：夜钓串钩底钓', '鱼种图鉴', 11, 1, 3, '2026-03-03 03:45:01.192323', '2026-03-03 03:45:01.192323', 0);


--
-- Data for Name: wiki_histories; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.wiki_histories (id, entry_id, content, user_id, version, created_at) VALUES (1, 3, '# 台钓基础\n\n台钓（悬坠钓法）是目前最流行的淡水钓法之一。\n\n## 基本装备\n- 台钓竿（3.6-6.3米）\n- 主线、子线\n- 浮漂、铅坠\n- 钓台或钓箱\n\n## 调漂方法\n1. 空钩半水调漂\n2. 确定调目（通常调4目）\n3. 挂饵找底\n4. 确定钓目（通常钓2目）
![图片](/api/uploads/images/f73f7f3b-7608-4d74-9705-3c87a4848c3a.jpg)
', 1, 2, '2026-02-27 01:26:10.070504');


--
-- Name: announcements_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.announcements_id_seq', 2, true);


--
-- Name: catch_records_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.catch_records_id_seq', 9, true);


--
-- Name: comments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.comments_id_seq', 54, true);


--
-- Name: favorites_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.favorites_id_seq', 1, true);


--
-- Name: fishing_spots_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.fishing_spots_id_seq', 24, true);


--
-- Name: follows_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.follows_id_seq', 1, true);


--
-- Name: gear_reviews_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gear_reviews_id_seq', 7, true);


--
-- Name: likes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.likes_id_seq', 1, true);


--
-- Name: messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.messages_id_seq', 1, false);


--
-- Name: notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.notifications_id_seq', 1, true);


--
-- Name: posts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.posts_id_seq', 60, true);


--
-- Name: reports_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.reports_id_seq', 1, false);


--
-- Name: sections_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sections_id_seq', 6, true);


--
-- Name: spot_reviews_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.spot_reviews_id_seq', 1, true);


--
-- Name: tags_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.tags_id_seq', 21, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.users_id_seq', 14, true);


--
-- Name: wiki_entries_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.wiki_entries_id_seq', 41, true);


--
-- Name: wiki_histories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.wiki_histories_id_seq', 1, true);


--
-- PostgreSQL database dump complete
--

\unrestrict OZLNXhMGf9AhbqSn5MEyu76thzuciUiiRcWaxphFF3897iQCjKTN5eKh0BHZeyV

