-- ============================================
-- 钓鱼爱好者网上交流系统 - 数据库建表脚本
-- 数据库: PostgreSQL
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    avatar VARCHAR(500) DEFAULT '/default-avatar.png',
    bio TEXT DEFAULT '',
    role VARCHAR(20) DEFAULT 'USER',  -- USER / ADMIN
    is_banned BOOLEAN DEFAULT FALSE,
    muted_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 板块/分区表
CREATE TABLE IF NOT EXISTS sections (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(100) DEFAULT '🎣',
    sort_order INTEGER DEFAULT 0,
    post_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 标签表
CREATE TABLE IF NOT EXISTS tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    section_id BIGINT REFERENCES sections(id),
    color VARCHAR(20) DEFAULT '#0ea5e9'
);

-- 帖子表
CREATE TABLE IF NOT EXISTS posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    section_id BIGINT NOT NULL REFERENCES sections(id),
    tag_id BIGINT,
    post_type VARCHAR(20) DEFAULT 'NORMAL',  -- NORMAL / CATCH / REVIEW
    is_top BOOLEAN DEFAULT FALSE,
    is_featured BOOLEAN DEFAULT FALSE,
    view_count INTEGER DEFAULT 0,
    like_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 帖子-标签关联表
CREATE TABLE IF NOT EXISTS post_tags (
    post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

-- 评论表（树形结构）
CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    parent_id BIGINT DEFAULT NULL REFERENCES comments(id) ON DELETE CASCADE,
    like_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 点赞表
CREATE TABLE IF NOT EXISTS likes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    target_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,  -- POST / COMMENT
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, target_id, target_type)
);

-- 收藏表
CREATE TABLE IF NOT EXISTS favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, post_id)
);

-- 关注表
CREATE TABLE IF NOT EXISTS follows (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL REFERENCES users(id),
    following_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(follower_id, following_id)
);

-- 私信表
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id),
    receiver_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(30) NOT NULL,  -- LIKE / COMMENT / FOLLOW / SYSTEM
    title VARCHAR(200),
    content TEXT,
    related_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 钓点表
CREATE TABLE IF NOT EXISTS fishing_spots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    fish_types VARCHAR(500),
    spot_type VARCHAR(50),           -- 水库 / 河流 / 湖泊 / 海钓 / 黑坑
    open_time VARCHAR(200),
    best_season VARCHAR(200),
    fee_info VARCHAR(200),
    no_fishing_notice TEXT,
    rating DOUBLE PRECISION DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    user_id BIGINT NOT NULL REFERENCES users(id),
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 钓点收藏表
CREATE TABLE IF NOT EXISTS spot_favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    spot_id BIGINT NOT NULL REFERENCES fishing_spots(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, spot_id)
);

-- 钓点评价表
CREATE TABLE IF NOT EXISTS spot_reviews (
    id BIGSERIAL PRIMARY KEY,
    spot_id BIGINT NOT NULL REFERENCES fishing_spots(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 知识百科词条表
CREATE TABLE IF NOT EXISTS wiki_entries (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100),           -- 鱼种 / 饵料 / 装备 / 技巧 / 常识
    user_id BIGINT NOT NULL REFERENCES users(id),
    version INTEGER DEFAULT 1,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 词条编辑历史表
CREATE TABLE IF NOT EXISTS wiki_histories (
    id BIGSERIAL PRIMARY KEY,
    entry_id BIGINT NOT NULL REFERENCES wiki_entries(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    version INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 百科独立讨论表
CREATE TABLE IF NOT EXISTS wiki_comments (
    id BIGSERIAL PRIMARY KEY,
    entry_id BIGINT NOT NULL REFERENCES wiki_entries(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    parent_id BIGINT DEFAULT NULL REFERENCES wiki_comments(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    like_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 举报表
CREATE TABLE IF NOT EXISTS reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    target_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,  -- POST / COMMENT / USER
    reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING / RESOLVED / REJECTED
    review_note TEXT,
    handled_by BIGINT REFERENCES users(id),
    handled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 管理员操作日志
CREATE TABLE IF NOT EXISTS admin_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NOT NULL REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(30),
    target_id BIGINT,
    detail TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 敏感词
CREATE TABLE IF NOT EXISTS sensitive_words (
    id BIGSERIAL PRIMARY KEY,
    word VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 公告表
CREATE TABLE IF NOT EXISTS announcements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 渔获日记表（帖子扩展表）
CREATE TABLE IF NOT EXISTS catch_records (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    fish_species VARCHAR(100),       -- 鱼种
    weight DOUBLE PRECISION,         -- 重量(斤)
    length DOUBLE PRECISION,         -- 长度(cm)
    bait VARCHAR(200),               -- 饵料
    spot_name VARCHAR(200),          -- 钓点名称
    weather VARCHAR(100),            -- 天气
    photo_url VARCHAR(500),          -- 渔获照片
    fishing_date DATE,               -- 垂钓日期
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 装备测评表（帖子扩展表）
CREATE TABLE IF NOT EXISTS gear_reviews (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    brand VARCHAR(100),              -- 品牌
    model VARCHAR(200),              -- 型号
    gear_category VARCHAR(50),       -- 分类: 鱼竿/鱼线/鱼钩/浮漂/饵料/其他
    price DOUBLE PRECISION,          -- 价格(元)
    rating INTEGER CHECK (rating BETWEEN 1 AND 5), -- 评分1-5
    pros TEXT,                       -- 优点
    cons TEXT,                       -- 缺点
    photo_url VARCHAR(500),          -- 装备照片
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 索引优化
-- ============================================
CREATE INDEX IF NOT EXISTS idx_posts_section ON posts(section_id);
CREATE INDEX IF NOT EXISTS idx_posts_user ON posts(user_id);
CREATE INDEX IF NOT EXISTS idx_posts_created ON posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_type ON posts(post_type);
CREATE INDEX IF NOT EXISTS idx_posts_tag ON posts(tag_id);
CREATE INDEX IF NOT EXISTS idx_posts_views ON posts(view_count DESC);
CREATE INDEX IF NOT EXISTS idx_catch_records_post ON catch_records(post_id);
CREATE INDEX IF NOT EXISTS idx_gear_reviews_post ON gear_reviews(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_post ON comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_user ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_wiki_comments_entry ON wiki_comments(entry_id);
CREATE INDEX IF NOT EXISTS idx_wiki_comments_user ON wiki_comments(user_id);
CREATE INDEX IF NOT EXISTS idx_spot_favorites_user ON spot_favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_messages_receiver ON messages(receiver_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_follows_follower ON follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following ON follows(following_id);
CREATE INDEX IF NOT EXISTS idx_fishing_spots_location ON fishing_spots(latitude, longitude);
