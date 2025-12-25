CREATE SCHEMA IF NOT EXISTS myblog;

CREATE TABLE IF NOT EXISTS myblog.posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    text VARCHAR(255) NOT NULL,
    tags VARCHAR(255)[] NOT NULL,
    likes_count INT DEFAULT 0 NOT NULL
    );
ALTER TABLE myblog.posts OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS myblog.comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    text TEXT NOT NULL,

    CONSTRAINT fk_comments_posts
    FOREIGN KEY (post_id)
    REFERENCES myblog.posts(id)
    ON DELETE CASCADE
    );
ALTER TABLE myblog.comments OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS myblog.images (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    image bytea NOT NULL,

    CONSTRAINT fk_images_posts
    FOREIGN KEY (post_id)
    REFERENCES myblog.posts(id)
);
ALTER TABLE myblog.images OWNER TO "postgres";
