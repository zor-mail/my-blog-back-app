package ru.yandex.practica.repositories;

import ru.yandex.practica.models.Post;

import java.util.List;

public interface PostsRepository {
    List<Post> findAll();
    void save(Post post);
    void deleteById(Long id);
    Boolean exists(Long id);
    void update(Long id, Post post);
}
