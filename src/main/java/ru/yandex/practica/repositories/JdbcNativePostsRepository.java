package ru.yandex.practica.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;


import ru.yandex.practica.models.Post;

@Repository
public class JdbcNativePostsRepository implements PostsRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Post> findAll() {
        // Выполняем запрос с помощью JdbcTemplate
        // Преобразовываем ответ с помощью RowMapper
        return jdbcTemplate.query(
                "select id, title, text, tags, likes_count from myblog.posts",
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getString("tags"),
                        rs.getInt("likes_count")
                ));
    }

    @Override
    public void save(Post post) {
        // Формируем insert-запрос с параметрами
        jdbcTemplate.update("insert into myblog.posts(title, text, tags) values(?, ?, ?, ?)",
                post.getTitle(), post.getText(), post.getTags());
    }

    @Override
    public void update(Long id, Post post) {
        jdbcTemplate.update("update myblog.posts set title = ?, text = ?, tags = ? where id = ?",
                post.getTitle(), post.getText(), post.getTags(), id);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("delete from myblog.posts where id = ?", id);
    }

    @Override
    public Boolean exists(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from myblog.posts where id = ?",
                Integer.class,
                id
        );
        return count != null && count > 0;
    }
}

