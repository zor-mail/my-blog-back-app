CREATE DATABASE "myblogdb" WITH TEMPLATE = template0 ENCODING = 'UTF8'
LC_COLLATE = 'ru_RU.UTF-8' LC_CTYPE = 'ru_RU.UTF-8' TABLESPACE pg_default;
ALTER DATABASE "myblogdb" OWNER TO "postgres";

CREATE SCHEMA "myblog";
ALTER SCHEMA "myblog" OWNER TO "postgres";

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
    text TEXT NOT NULL
    );
ALTER TABLE myblog.comments OWNER TO "postgres";

ALTER TABLE ONLY myblog.comments
    ADD CONSTRAINT "posts_comments_fkey" FOREIGN KEY ("post_id")
    REFERENCES myblog.posts("id") ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS myblog.images (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    image bytea NOT NULL
);
ALTER TABLE myblog.images OWNER TO "postgres";

ALTER TABLE ONLY myblog.images
    ADD CONSTRAINT "posts_images_fkey" FOREIGN KEY ("post_id")
    REFERENCES myblog.posts("id");