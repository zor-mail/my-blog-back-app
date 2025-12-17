package ru.yandex.practica.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.Post;
import ru.yandex.practica.repositories.PostsRepository;

import java.util.List;


    @Service
    public class PostsService {

        private final PostsRepository postsRepository;

        public PostsService(PostsRepository postsRepository) {
            this.postsRepository = postsRepository;
        }

        public Post getPost(
                Long postId
        ) {
            return postsRepository.getPost(postId);
        }

        public List<Post> getPosts(
                String search,
                Integer pageNumber,
                Integer pageSize
        ) {
            return postsRepository.getPosts(search, pageNumber, pageSize);
        }

        public void addPost(Post post) {
            postsRepository.addPost(post);
        }

        public void updatePost(Long id, Post post) {
            postsRepository.updatePost(id, post);
        }

        public void deletePost(Long id) {
            postsRepository.deletePost(id);
        }


        // Likes
        //==============================================
        public void addLike() {
            postsRepository.addLike();
        }


        // Images
        //==============================================
        byte[] getImage(Long postId) {
            postsRepository.getImage(postId);
        }

        void updateImage(Long postId, MultipartFile image) {
            byte[] imageBytes = null;
            postsRepository.updateImage(postId, imageBytes);
        }

        // Comments
        //==============================================
        public Comment getComment(Long commentId) {
            postsRepository.getComment(commentId);
        }

        public List<Comment> getComments(Long postId) {
            return postsRepository.getComments(postId);
        }

        public void addComment(Long postId, Comment comment) {
            postsRepository.addComment(postId, comment);
        }

        public void updateComment(Long commentId, Comment comment) {
            postsRepository.updateComment(commentId, comment);
        }

        public void deleteComment(Long id) {
            postsRepository.deleteComment(id);
        }

    }
