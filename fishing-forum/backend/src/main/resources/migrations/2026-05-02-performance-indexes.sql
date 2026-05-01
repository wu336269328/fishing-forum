-- 性能优化：补全热点查询路径的二级索引
-- 由 DatabaseMigrationRunner 自动幂等执行；CREATE INDEX IF NOT EXISTS 在 Postgres 9.5+ 支持

-- 帖子列表：板块/类型/作者维度筛选 + 时间排序
CREATE INDEX IF NOT EXISTS idx_posts_section_created ON posts(section_id, created_at DESC) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_posts_user_created    ON posts(user_id, created_at DESC) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_posts_type_created    ON posts(post_type, created_at DESC) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_posts_tag_created     ON posts(tag_id, created_at DESC) WHERE deleted = 0 AND tag_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_posts_hot             ON posts(is_top DESC, view_count DESC, like_count DESC) WHERE deleted = 0;

-- 帖子-标签关联反向查询
CREATE INDEX IF NOT EXISTS idx_post_tags_tag        ON post_tags(tag_id);

-- 评论按帖子聚合 + 子评论
CREATE INDEX IF NOT EXISTS idx_comments_post_created ON comments(post_id, created_at) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_comments_parent       ON comments(parent_id) WHERE parent_id IS NOT NULL AND deleted = 0;
CREATE INDEX IF NOT EXISTS idx_comments_user         ON comments(user_id) WHERE deleted = 0;

-- 点赞反向查询：某帖/某评论被多少人赞
CREATE INDEX IF NOT EXISTS idx_likes_target          ON likes(target_id, target_type);

-- 关注：拉某人粉丝列表
CREATE INDEX IF NOT EXISTS idx_follows_following     ON follows(following_id);

-- 收藏：列出某用户的收藏
CREATE INDEX IF NOT EXISTS idx_favorites_user_created ON favorites(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_favorites_post        ON favorites(post_id);

-- 私信：会话列表与未读数
CREATE INDEX IF NOT EXISTS idx_messages_pair_created  ON messages(sender_id, receiver_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_receiver_unread ON messages(receiver_id, is_read, created_at DESC);

-- 通知：用户拉未读
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, is_read, created_at DESC);

-- 钓点：按类型筛选
CREATE INDEX IF NOT EXISTS idx_spots_type            ON fishing_spots(spot_type);
CREATE INDEX IF NOT EXISTS idx_spots_created         ON fishing_spots(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_spot_reviews_spot     ON spot_reviews(spot_id, created_at DESC);

-- 百科：分类筛选 + 词条评论
CREATE INDEX IF NOT EXISTS idx_wiki_category         ON wiki_entries(category, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_wiki_comments_entry   ON wiki_comments(entry_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_wiki_histories_entry  ON wiki_histories(entry_id, created_at DESC);

-- 举报：管理员按状态筛
CREATE INDEX IF NOT EXISTS idx_reports_status_created ON reports(status, created_at DESC);

-- 渔获/装备测评：按帖子拉
CREATE INDEX IF NOT EXISTS idx_catch_records_post   ON catch_records(post_id);
CREATE INDEX IF NOT EXISTS idx_gear_reviews_post    ON gear_reviews(post_id);
