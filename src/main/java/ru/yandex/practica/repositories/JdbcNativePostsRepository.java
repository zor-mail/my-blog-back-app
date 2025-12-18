package ru.yandex.practica.repositories;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Blob;
import java.util.*;
import java.util.stream.Collectors;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.Post;

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
                stream().findFirst().orElse(null);
    }


    @Override
    public Post getPost(Long postId) {
        return jdbcTemplate.query(
                "select id, title, text, tags, likes_count from myblog.posts where id = ?",
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getString("tags"),
                        rs.getInt("likes_count")
                ), postId).stream().findFirst().orElse(null);
    }


    @Override
    public List<Post> getPosts(
            String whereCondition,
            Long offset
    ) {
        String selectString = String.format("select id, title, text, tags, likes_count from myblog.posts %s OFFSET %d",
                whereCondition, offset);
        return jdbcTemplate.query(selectString,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getString("tags"),
                        rs.getInt("likes_count")
                ));
    }

    @Override
    public void addPost(Post post) {
        // Формируем insert-запрос с параметрами
        jdbcTemplate.update("insert into myblog.posts(title, text, tags) values(?, ?, ?, ?)",
                post.getTitle(), post.getText(), post.getTags());
    }

    @Override
    public void updatePost(Long id, Post post) {
        jdbcTemplate.update("update myblog.posts set title = ?, text = ?, tags = ? where id = ?",
                post.getTitle(), post.getText(), post.getTags(), id);
    }

    @Override
    public void deletePost(Long id) {
        jdbcTemplate.update("delete from myblog.posts where id = ?", id);
    }


    // Likes
    //==============================================
   public void addLike(Long postId) {
       jdbcTemplate.update("update myblog.posts set likesCount = likesCount + 1 where post_id = ?",
               postId);
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
    public void addComment(Long postId, Comment comment) {
        // Формируем insert-запрос с параметрами
        jdbcTemplate.update("insert into myblog.comments(post_id, text) values(?, ?)",
                comment.getPostId(), comment.getText());
    }

    @Override
    public void updateComment(Long commentId, Comment comment) {
        jdbcTemplate.update("update myblog.comments set post_id = ?, text = ? where id = ?",
                comment.getPostId(), comment.getText(), commentId);
    }

    @Override
    public void deleteComment(Long commentId) {
        jdbcTemplate.update("delete from myblog.comments where id = ?", commentId);
    }
    
    
    
    
    


    /*@ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        service.deleteById(id);
    }

    @PostMapping(path = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(@PathVariable("id") Long id,
                                               @RequestParam("file") MultipartFile file) throws Exception {
        if (!service.exists(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found");
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("empty file");
        }
        boolean ok = service.uploadAvatar(id, file.getBytes());
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to update avatar");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("ok");
    }*/
    
    
    
    
}

