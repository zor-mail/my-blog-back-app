package ru.yandex.practica.repositories;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Blob;
import java.util.*;
import java.util.stream.Collectors;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.Post;
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
    public Long getReconrdsCount(String searchCondition) {
        return jdbcTemplate.query(
                "select count(*) as counter from myblog.posts" + searchCondition,
                (rs, rowNum) -> rs.getLong("counter"), Long.class).
                stream().findFirst().orElse(0L);
    }


    @Override
    public PostDTO getPost(Long postId) {
                String selectString =
                        "SELECT " +
                                " ps.*," +
                                " COALESCE(comm.comments_count, 0) AS counter" +
                                " FROM posts ps" +
                                " LEFT JOIN (" +
                                "    SELECT post_id, COUNT(*) AS comments_count" +
                                "    FROM comments" +
                                "    GROUP BY post_id" +
                                ") comm" +
                                " ON ps.id = comm.post_id" +
                                " where us.id = ?";

        return jdbcTemplate.query(selectString,
                (rs, rowNum) -> new PostDTO(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getString("tags"),
                        rs.getInt("likes_count"),
                        rs.getInt("comments_count")
                ), postId).stream().findFirst().orElse(null);
    }


    @Override
    public List<PostDTO> getPosts(
            String whereCondition,
            Long offset
    ) {
        String selectString = String.format(
                "SELECT " +
                        " ps.*," +
                        " COALESCE(comm.comments_count, 0) AS counter" +
                        " FROM posts ps" +
                        " LEFT JOIN (" +
                        "    SELECT post_id, COUNT(*) AS comments_count" +
                        "    FROM comments" +
                        "    GROUP BY post_id" +
                        ") comm" +
                        " ON ps.id = comm.post_id" +
                " %s OFFSET %d",
                whereCondition, offset);

        return jdbcTemplate.query(selectString,
                (rs, rowNum) -> new PostDTO(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getString("tags"),
                        rs.getInt("likes_count"),
                        rs.getInt("comments_count"))
                );
    }

    @Override
    public PostDTO addPost(PostDTO post) {
        String sqlTemplate = "insert into myblog.posts(title, text, tags) values(?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(sqlTemplate, new String[]{"id"});
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            String tagsString = String.join(" ", post.getTags());
            ps.setString(3, tagsString);
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
        jdbcTemplate.update("update myblog.posts set title = ?, text = ?, tags = ? where id = ?",
                post.getTitle(), post.getText(), post.getTags(), post.getId());
        return post;
    }

    @Override
    public void deletePost(Long id) {
        jdbcTemplate.update("delete from myblog.posts where id = ?", id);
    }


    // Likes
    //==============================================
    @Transactional
    public Integer addLike(Long postId) {
        jdbcTemplate.update(
                "update myblog.posts set likes_count = likes_count + 1 where post_id = ?",
                postId
        );

        return jdbcTemplate.queryForObject(
                "select likes_count from myblog.posts where post_id = ?",
                Integer.class,
                postId
        );
    }


    // Images
    //==============================================
    public byte[] getImage(Long postId) {
        return jdbcTemplate.query("select image from myblog.images where post_id = ?",
                (rs, rowNum) ->  rs.getBytes("image"),
                postId).stream().findFirst().orElse(null);
    }

    public void updateImage(Long postId, byte[] imageBytes) {
            jdbcTemplate.update("update myblog.images set image = ? where post_id = ?",
                    imageBytes, postId);
    }

    // Comments
    //==============================================

    @Override
    public Comment getComment(Long commentId) {
        return jdbcTemplate.query(
                "select id, post_id, text from myblog.comments where id = ?",
                (rs, rowNum) -> new Comment(
                        rs.getLong("id"),
                        rs.getLong("post_id"),
                        rs.getString("text")
                ), commentId).stream().findFirst().orElse(null);
    }

    @Override
    public List<Comment> getComments(Long postId) {
        return jdbcTemplate.query(
                "select id, title, text from myblog.comments where post_id = ?",
                (rs, rowNum) -> new Comment(
                        rs.getLong("id"),
                        rs.getLong("postId"),
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
    public void deleteComment(Long commentId) {
        jdbcTemplate.update("delete from myblog.comments where id = ?", commentId);
    }
}

