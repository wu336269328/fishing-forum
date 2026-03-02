-- ============================================
-- 钓鱼论坛 - 初始数据
-- ============================================

-- 管理员账号（密码: admin123，BCrypt加密）
INSERT INTO users (username, password, email, avatar, bio, role)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8Co/a', 'admin@fishforum.com', '/default-avatar.png', '系统管理员', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- 测试用户（密码: user123）
INSERT INTO users (username, password, email, avatar, bio, role)
VALUES ('钓鱼达人', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8Co/a', 'fisher@fishforum.com', '/default-avatar.png', '热爱钓鱼的老玩家', 'USER')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, email, avatar, bio, role)
VALUES ('野钓小王', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8Co/a', 'wang@fishforum.com', '/default-avatar.png', '专注野钓二十年', 'USER')
ON CONFLICT (username) DO NOTHING;

-- 论坛板块
INSERT INTO sections (name, description, icon, sort_order) VALUES
('综合交流', '钓鱼爱好者综合交流区，分享日常钓鱼趣事', '💬', 1),
('渔获分享', '晒出你的战利品！分享每次出钓的收获', '🐟', 2),
('装备讨论', '钓竿、鱼线、鱼饵等装备的讨论与推荐', '🎣', 3),
('技巧攻略', '分享各种钓鱼技巧、方法和攻略', '📖', 4),
('钓点推荐', '推荐好的钓鱼地点，交流钓场信息', '📍', 5),
('赛事活动', '钓鱼比赛、线下活动信息发布', '🏆', 6)
ON CONFLICT DO NOTHING;

-- 标签
INSERT INTO tags (name, section_id, color) VALUES
('台钓', 1, '#0ea5e9'),
('路亚', 1, '#8b5cf6'),
('海钓', 1, '#f59e0b'),
('野钓', 1, '#10b981'),
('黑坑', 1, '#ef4444'),
('鲤鱼', 2, '#0ea5e9'),
('鲫鱼', 2, '#10b981'),
('鲈鱼', 2, '#8b5cf6'),
('鳜鱼', 2, '#f59e0b'),
('翘嘴', 2, '#ec4899'),
('鱼竿', 3, '#0ea5e9'),
('鱼线', 3, '#10b981'),
('饵料', 3, '#f59e0b'),
('浮漂', 3, '#8b5cf6'),
('鱼钩', 3, '#ef4444'),
('草鱼', NULL, '#22c55e'),
('鲢鳙', NULL, '#06b6d4'),
('黑鱼', NULL, '#6366f1'),
('青鱼', NULL, '#14b8a6'),
('鲶鱼', NULL, '#f97316'),
('黄颡鱼', NULL, '#eab308')
ON CONFLICT DO NOTHING;

-- 示例帖子
INSERT INTO posts (title, content, user_id, section_id, view_count, like_count, comment_count) VALUES
('今天野钓鲫鱼大丰收！', '<p>今天去水库野钓，连竿上鱼，总共钓了十几条鲫鱼，最大的有一斤多！用的4.5米的竿子，酒米打窝，红虫挂钩。天气不错，微风，水温大概12度左右。</p><p>分享一下今天的渔获，希望大家也能有好的收获！🎣</p>', 2, 2, 156, 23, 5),
('新手入门：台钓装备选择指南', '<p>很多新手刚入坑不知道怎么选装备，我来分享一下个人经验：</p><h3>鱼竿选择</h3><p>新手建议从3.6米或4.5米的综合竿开始，调性选择28调，手感好且不容易断。品牌推荐光威、化氏等性价比高的国产品牌。</p><h3>线组搭配</h3><p>主线1.0+子线0.6，适合钓鲫鱼。如果目标鱼较大，可以适当加粗。</p><h3>浮漂选择</h3><p>巴尔杉木浮漂稳定性好，吃铅量1-2g适合静水作钓。</p>', 3, 3, 342, 45, 12),
('分享一个绝佳野钓点位', '<p>最近发现一个很好的野钓点位，水质清澈，鱼种丰富，有鲫鱼、鲤鱼、草鱼等。周围环境也很好，适合周末家庭出行。</p><p>具体位置在城郊的一个小水库，交通方便，停车也不难。建议大家早去占位，周末人比较多。</p>', 2, 5, 98, 15, 3)
ON CONFLICT DO NOTHING;

-- 示例评论
INSERT INTO comments (content, post_id, user_id, parent_id) VALUES
('太厉害了！请问用的什么饵料？', 1, 3, NULL),
('红虫效果确实好，冬天必备', 1, 2, 1),
('收藏了！新手必读', 2, 2, NULL),
('写得很详细，感谢分享', 2, 3, NULL),
('请问具体在哪个位置呀？', 3, 3, NULL)
ON CONFLICT DO NOTHING;

-- 示例钓点
INSERT INTO fishing_spots (name, description, latitude, longitude, fish_types, spot_type, open_time, rating, user_id) VALUES
('翠湖水库', '环境优美的中型水库，水质清澈，鱼种丰富。适合台钓和路亚。周边有停车场和简易卫生间。', 30.5728, 104.0668, '鲫鱼,鲤鱼,草鱼,鲢鳙', '水库', '全天开放', 4.5, 2),
('锦江河段', '市区内的一段河流钓场，交通便利。主要鱼种为鲫鱼和鲤鱼，偶尔能钓到翘嘴。', 30.5726, 104.0665, '鲫鱼,鲤鱼,翘嘴', '河流', '全天开放', 3.8, 3),
('青龙湖', '大型湖泊，适合船钓和岸钓。鱼种众多，秋季鲈鱼活跃。需要购买钓鱼证。', 30.5730, 104.0670, '鲈鱼,鲤鱼,鲫鱼,鳜鱼', '湖泊', '06:00-20:00', 4.2, 2)
ON CONFLICT DO NOTHING;

-- 示例百科词条
INSERT INTO wiki_entries (title, content, category, user_id, version) VALUES
('鲫鱼', '# 鲫鱼\n\n鲫鱼（学名：Carassius auratus）是淡水鱼中最常见的鱼种之一，也是钓鱼爱好者最常钓获的鱼种。\n\n## 生活习性\n- 栖息于水体底层\n- 杂食性，偏爱植物性饵料\n- 耐低温，冬季仍可垂钓\n\n## 钓鱼技巧\n- **饵料选择**：春夏用商品饵（腥香型），秋冬用红虫、蚯蚓\n- **钓组搭配**：主线0.8-1.2号，子线0.4-0.6号\n- **调漂方法**：调四钓二适合大多数情况', '鱼种', 2, 1),
('蚯蚓饵', '# 蚯蚓饵\n\n蚯蚓是最经典的钓鱼饵料之一，被称为"万能饵"。\n\n## 适用范围\n适合钓鲫鱼、鲤鱼、黄颡鱼、鲶鱼等多种鱼类。\n\n## 使用方法\n1. 选择红色蚯蚓，活力好的效果更佳\n2. 穿钩时不要露出钩尖太多\n3. 可以蘸一些商品饵粉增加诱鱼效果', '饵料', 3, 1),
('台钓基础', '# 台钓基础\n\n台钓（悬坠钓法）是目前最流行的淡水钓法之一。\n\n## 基本装备\n- 台钓竿（3.6-6.3米）\n- 主线、子线\n- 浮漂、铅坠\n- 钓台或钓箱\n\n## 调漂方法\n1. 空钩半水调漂\n2. 确定调目（通常调4目）\n3. 挂饵找底\n4. 确定钓目（通常钓2目）', '技巧', 2, 1)
ON CONFLICT DO NOTHING;

-- 公告
INSERT INTO announcements (title, content, is_active) VALUES
('欢迎来到钓鱼论坛！', '欢迎各位钓友加入我们的社区！在这里你可以分享渔获、交流技巧、推荐钓点。请遵守社区规则，文明交流。祝大家天天爆护！🎣', TRUE),
('社区规则公告', '为维护良好的社区氛围，请大家遵守以下规则：1. 文明用语，友善交流；2. 禁止发布广告和虚假信息；3. 尊重他人隐私，不随意转发他人信息；4. 举报违规内容，共建美好社区。', TRUE)
ON CONFLICT DO NOTHING;
