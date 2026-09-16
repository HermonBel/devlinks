CREATE TABLE bookmark_tags (
                               bookmark_id BIGINT NOT NULL REFERENCES bookmarks(id) ON DELETE CASCADE,
                               tag_id      BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
                               PRIMARY KEY (bookmark_id, tag_id)
);

CREATE INDEX idx_bookmark_tags_tag_id ON bookmark_tags(tag_id);