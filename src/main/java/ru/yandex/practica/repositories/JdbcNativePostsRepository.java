package ru.yandex.practica.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.util.*;

import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.PostDTO;

@Repository
public class JdbcNativePostsRepository implements PostsRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Posts
    //==============================================
    @Override
    public Long getRecordsCount(String searchCondition) {
        return jdbcTemplate.queryForObject(
                "select count(*) as counter from myblog.posts" + searchCondition, Long.class);
    }

    @Override
    public PostDTO getPost(Long postId) {
                String selectString =
                        "SELECT " +
                                " ps.*," +
                                " COALESCE(comm.comments_count, 0) AS comments_count" +
                                " FROM myblog.posts ps" +
                                " LEFT JOIN (" +
                                "    SELECT post_id, COUNT(*) AS comments_count" +
                                "    FROM myblog.comments" +
                                "    GROUP BY post_id" +
                                ") comm" +
                                " ON ps.id = comm.post_id" +
                                " where ps.id = ?";

        return jdbcTemplate.queryForObject(selectString,
                (rs, rowNum) -> {
                    String tagsStr = rs.getString("tags"); // "#Питер,#белыеночи"
                    String[] tagsArray = tagsStr == null || tagsStr.isBlank()
                            ? new String[0]
                            : tagsStr.split(",");
                    tagsArray = Arrays.stream(tagsArray)
                            .map(String::trim)
                            .toArray(String[]::new);
                    return new PostDTO(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("text"),
                            tagsArray,
                            rs.getInt("likes_count"),
                            rs.getInt("comments_count")
                    );
                }, postId);
    }

    @Override
    public List<PostDTO> getPosts(
            String whereCondition,
            Integer pageSize,
            Long offset
    ) {
        String selectString = String.format(
                "SELECT " +
                        " ps.id, " +
                        " ps.title," +
                        " CASE WHEN" +
                        " LENGTH(ps.text) > 128" +
                        " THEN" +
                        " left(ps.text, 128) || '...'" +
                        " ELSE" +
                        " ps.text END as text, " +
                        " ps.tags," +
                        " ps.likes_count," +
                        " COALESCE(comm.comments_count, 0) AS comments_count" +
                        " FROM myblog.posts ps" +
                        " LEFT JOIN (" +
                        "    SELECT post_id, COUNT(*) AS comments_count" +
                        "    FROM myblog.comments" +
                        "    GROUP BY post_id" +
                        ") comm" +
                        " ON ps.id = comm.post_id" +
                " %s LIMIT ? OFFSET ?", whereCondition);

        return jdbcTemplate.query(selectString,
                (rs, rowNum) -> {
                    String tagsStr = rs.getString("tags"); // "#Питер,#белыеночи"
                    String[] tagsArray = tagsStr == null || tagsStr.isBlank()
                            ? new String[0]
                            : tagsStr.split(",");
                    tagsArray = Arrays.stream(tagsArray)
                            .map(String::trim)
                            .toArray(String[]::new);
                        return new PostDTO(
                                rs.getLong("id"),
                                rs.getString("title"),
                                rs.getString("text"),
                                tagsArray,
                                rs.getInt("likes_count"),
                                rs.getInt("comments_count")
                        );
                }, pageSize, offset
        );
    }

    @Override
    public PostDTO addPost(PostDTO post) {
        String sqlTemplate = "insert into myblog.posts(title, text, tags) values(?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(sqlTemplate, new String[]{"id"});
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            ps.setString(3, String.join(",", post.getTags()));
            return ps;
        }, keyHolder);
        Long postId = keyHolder.getKey().longValue();
        post.setId(postId);
        post.setLikesCount(0);
        post.setCommentsCount(0);
        return post;
    }

    @Override
    public PostDTO updatePost(PostDTO post) {
        String sqlTemplate = "update myblog.posts set title = ?, text = ?, tags = ? where id = ?";

        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(sqlTemplate, new String[]{"id"});
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            ps.setString(3, String.join(",", post.getTags()));
            ps.setLong(4, post.getId());
            return ps;
        });
        return getPost(post.getId());
    }

    @Override
    public Integer deletePost(Long id) {
        return jdbcTemplate.update("delete from myblog.posts where id = ?", id);
    }


    // Likes
    //==============================================
    @Transactional
    public Integer addLike(Long postId) {
        jdbcTemplate.update(
                "update myblog.posts set likes_count = likes_count + 1 where id = ?",
                postId
        );

        return jdbcTemplate.queryForObject(
                "select likes_count from myblog.posts where id = ?",
                Integer.class,
                postId
        );
    }


    // Images
    //==============================================
    public byte[] getImage(Long postId) {
        List<byte[]> list = jdbcTemplate.query(
                "select image from myblog.images where post_id = ?",
                (rs, rowNum) -> rs.getBytes("image"),
                postId
        );
        return list.isEmpty() ? null : list.getFirst();
    }

    public Integer updateImage(Long postId, String fileName, byte[] imageBytes) {

        Integer rowsCount = jdbcTemplate.queryForObject(
                "select count(*) from myblog.posts where id = ?",
                Integer.class,
                postId
        );

        if (rowsCount == null || rowsCount == 0)
            return null;

        int rows = jdbcTemplate.update(
                "update myblog.images set filename = ?, image = ? where post_id = ?",
                fileName, imageBytes, postId
        );

        if (rows == 0) {
            rows = jdbcTemplate.update(
                    "insert into myblog.images (post_id, filename, image) values (?, ?, ?)",
                    postId, fileName, imageBytes
            );
        }
        return rows;
    }

    // Comments
    //==============================================
    @Override
    public Comment getComment(Long commentId) {
        return jdbcTemplate.queryForObject(
                "select id, post_id, text from myblog.comments where id = ?",
                (rs, rowNum) -> new Comment(
                        rs.getLong("id"),
                        rs.getLong("post_id"),
                        rs.getString("text")
                ), commentId);
    }

    @Override
    public List<Comment> getComments(Long postId) {
        return jdbcTemplate.query(
                "select id, post_id, text from myblog.comments where post_id = ?",
                (rs, rowNum) -> new Comment(
                        rs.getLong("id"),
                        rs.getLong("post_id"),
                        rs.getString("text")
                ), postId);
    }

    @Override
    public Comment addComment(Comment comment) {
        String sqlTemplate = "insert into myblog.comments(post_id, text) values(?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(sqlTemplate, new String[]{"id"});
            ps.setLong(1, comment.getPostId());
            ps.setString(2, comment.getText());
            return ps;
        }, keyHolder);
        Long commentId = keyHolder.getKey().longValue();
        comment.setId(commentId);
        return comment;
    }

    @Override
    public Comment updateComment(Comment comment) {
        jdbcTemplate.update("update myblog.comments set text = ? where id = ?",
                comment.getText(), comment.getId());
        return comment;
    }

    @Override
    public Integer deleteComment(Long commentId) {
        return jdbcTemplate.update("delete from myblog.comments where id = ?", commentId);
    }
}

