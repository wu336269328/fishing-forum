ALTER TABLE fishing_spots ADD COLUMN IF NOT EXISTS best_season VARCHAR(200);
ALTER TABLE fishing_spots ADD COLUMN IF NOT EXISTS fee_info VARCHAR(200);
ALTER TABLE fishing_spots ADD COLUMN IF NOT EXISTS no_fishing_notice TEXT;

CREATE TABLE IF NOT EXISTS spot_favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    spot_id BIGINT NOT NULL REFERENCES fishing_spots(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, spot_id)
);

CREATE INDEX IF NOT EXISTS idx_spot_favorites_user ON spot_favorites(user_id);
