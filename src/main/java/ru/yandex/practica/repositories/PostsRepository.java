package ru.yandex.practica.repositories;

import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.PostDTO;

import java.util.List;

public interface PostsRepository {

    Long getRecordsCount(String whereCondition);
    PostDTO getPost(Long postId);
    List<PostDTO> getPosts(
            String search,
            Integer pageSize,
            Long offset);
    PostDTO addPost(PostDTO post);
    void deletePost(Long id);
    PostDTO updatePost(PostDTO post);
    Integer addLike(Long postId);
    byte[] getImage(Long postId);
    void updateImage(Long postId, String fileName, byte[] imageBytes);

    Comment getComment(Long commentId);
    List<Comment> getComments(Long postId);
    Comment addComment(Comment comment);
    void deleteComment(Long commentId);
    Comment updateComment(Comment comment);
}
