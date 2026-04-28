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

CREATE INDEX IF NOT EXISTS idx_wiki_comments_entry ON wiki_comments(entry_id);
CREATE INDEX IF NOT EXISTS idx_wiki_comments_user ON wiki_comments(user_id);
