-- Add the column, initially nullable so existing rows are valid
ALTER TABLE bookmarks ADD COLUMN user_id BIGINT;

-- Backfill: assign all existing bookmarks to the first user (id=1)
-- In a real migration you'd handle this more carefully; here we know you have test data
UPDATE bookmarks SET user_id = (SELECT MIN(id) FROM users) WHERE user_id IS NULL;

-- Now that all rows have a value, enforce NOT NULL
ALTER TABLE bookmarks ALTER COLUMN user_id SET NOT NULL;

-- Foreign key constraint
ALTER TABLE bookmarks
    ADD CONSTRAINT fk_bookmarks_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Index for fast lookups: WHERE user_id = ?
CREATE INDEX idx_bookmarks_user_id ON bookmarks(user_id);