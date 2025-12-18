package ru.yandex.practica.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.Post;
import ru.yandex.practica.models.PostsDTO;
import ru.yandex.practica.repositories.PostsRepository;

import java.io.IOException;
import java.sql.Blob;
import java.util.ArrayList;
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

        public PostsDTO getPosts(
                String search,
                Integer pageNumber,
                Integer pageSize
        ) {

            String whereCondition = getTagsAndWordsSearchString(search, "title");
            Long recordsCount = postsRepository.getReconrdsCount(whereCondition);
            int lastPage = (int)Math.ceil((double) recordsCount / pageSize);
            boolean hasPrev = pageNumber != 1;
            boolean hasNext = pageNumber < lastPage;
            long offset = (long) pageSize * (pageNumber - 1);
            if (offset >= recordsCount)
                return null;
            List<Post> posts = postsRepository.getPosts(whereCondition, offset);
            return new PostsDTO(posts, hasPrev, hasNext, lastPage);
        }

        String getTagsAndWordsSearchString(String search, String searchColumnName) {
            StringBuilder resultSearchBuilder = new StringBuilder();
            StringBuilder wordsCollector = new StringBuilder();
            String[] splitted = search.split("\\s+"); // для пробелов, табов и т.п.
            String andLikeSearchPattern = "'% AND %s like %'";
            for (String searchWord : splitted) {
                if (searchWord.equals("#"))
                    continue;
                if (searchWord.startsWith("#")) {
                    if (!resultSearchBuilder.isEmpty())
                        resultSearchBuilder.append(String.format(andLikeSearchPattern, searchColumnName));
                    resultSearchBuilder.append(searchWord.substring(1));
                    if (!wordsCollector.isEmpty()) {
                        if (!resultSearchBuilder.isEmpty())
                            resultSearchBuilder.append(String.format(andLikeSearchPattern, searchColumnName));
                        resultSearchBuilder.append(wordsCollector);
                        wordsCollector = new StringBuilder();
                    }
                } else
                    wordsCollector.append(searchWord);
            }
            if (!wordsCollector.isEmpty()) {
                if (!resultSearchBuilder.isEmpty())
                    resultSearchBuilder.append(String.format(andLikeSearchPattern, searchColumnName));
                resultSearchBuilder.append(wordsCollector);
            }

            return resultSearchBuilder.isEmpty() ? "" : " WHERE " + resultSearchBuilder.toString();
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
        public void addLike(Long postId) {
            postsRepository.addLike(postId);
        }


        // Images
        //==============================================
        public byte[] getImage(Long postId) {
            return postsRepository.getImage(postId);
        }

        public void updateImage(Long postId, MultipartFile image) throws IOException {
            if (image == null)
                return;
            postsRepository.updateImage(postId, image.getBytes());
        }

        // Comments
        //==============================================
        public Comment getComment(Long commentId) {
            return postsRepository.getComment(commentId);
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
