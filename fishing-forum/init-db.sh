#!/bin/bash
# 初始化数据库后插入额外数据（鱼种标签等）
set -e

psql -v ON_ERROR_STOP=0 -U postgres -d fishing_forum <<'SQL'
-- 鱼种标签
INSERT INTO tags (name, description) VALUES
('草鱼', '草鱼相关'),('鲢鳙', '鲢鳙相关'),('黑鱼', '黑鱼相关'),
('青鱼', '青鱼相关'),('鲶鱼', '鲶鱼相关'),('黄颡鱼', '黄颡鱼相关')
ON CONFLICT DO NOTHING;
SQL

echo "数据库初始化完成！"
