CREATE TABLE bookmarks (
                           id          BIGSERIAL PRIMARY KEY,
                           title       VARCHAR(100) NOT NULL,
                           url         VARCHAR(500) NOT NULL,
                           description VARCHAR(2000)
);