package ru.yandex.practica.repositories;

import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.Post;

import java.util.List;

public interface PostsRepository {
    Post getPost(Long postId);
    List<Post> getPosts(
            String search,
            Integer pageNumber,
            Integer pageSize);
    void addPost(Post post);
    void deletePost(Long id);
    void updatePost(Long id, Post post);
    void addLike();
    byte[] getImage(Long postId);
    void updateImage(Long postId, byte[] imageBytes);

    Comment getComment(Long commentId);
    List<Comment> getComments(Long postId);
    void addComment(Long postId, Comment comment);
    void deleteComment(Long commentId);
    void updateComment(Long commentId, Comment comment);
}
