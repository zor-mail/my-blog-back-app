package ru.yandex.practica.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.Post;
import ru.yandex.practica.models.PostDTO;
import ru.yandex.practica.models.PostsDTO;
import ru.yandex.practica.repositories.PostsRepository;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
    public class PostsService {

        private final PostsRepository postsRepository;

        public PostsService(PostsRepository postsRepository) {
            this.postsRepository = postsRepository;
        }

        public PostDTO getPost(
                Long postId
        ) {
            return postsRepository.getPost(postId);
        }

        public PostsDTO getPosts(
                String searchString,
                Integer pageNumber,
                Integer pageSize
        ) {
            String whereCondition;
            if (searchString == null || searchString.isEmpty())
                whereCondition = "";
            else
                whereCondition = getTagsAndWordsSearchString(searchString, "title", "tags");

            Long recordsCount = postsRepository.getReconrdsCount(whereCondition);
            int lastPage = (int)Math.ceil((double) recordsCount / pageSize);
            boolean hasPrev = pageNumber != 1;
            boolean hasNext = pageNumber < lastPage;
            long offset = (long) pageSize * (pageNumber - 1);
            if (offset >= recordsCount)
                return null;
            List<PostDTO> posts = postsRepository.getPosts(whereCondition, offset);
            return new PostsDTO(posts, hasPrev, hasNext, lastPage);
        }

    public static String getTagsAndWordsSearchString(String searchString, String titleColumnName, String tagsColumnName) {
        StringBuilder resultSearchBuilder = new StringBuilder();
        String andWherePart = "%' AND ";
        String titleWhereCondition = String.format("%s like '%%", titleColumnName);
        String tagsWhereCondition = String.format("%s like '%%", tagsColumnName);
        String splittedUnitedWords;

        String splittedTags = Arrays.stream(searchString.split("\\s+")).
                filter(substr -> substr.startsWith("#") && substr.length() > 1).
                map(substr -> substr.substring(1)).
                collect(Collectors.joining(andWherePart + tagsWhereCondition));

        if (splittedTags.isEmpty())
            splittedUnitedWords = searchString;
        else {
            // убирается первое слово в каждом подмассиве, начинающееся с # (как тэг)
            splittedUnitedWords = Arrays.stream(("for_del " + searchString).split("#")).map(substr -> {
                        String[] substrArr = substr.split("\\s+");// для пробелов, табов и т.п.
                        if (substrArr.length < 2)
                            return null;
                        return String.join(" ", Arrays.copyOfRange(substrArr, 1, substrArr.length));
                    }).filter(substr -> substr != null && !substr.trim().isEmpty()).
                    collect(Collectors.joining(andWherePart + titleWhereCondition));
        }

        if (!splittedUnitedWords.isEmpty()) {
            resultSearchBuilder.append(titleWhereCondition);
            resultSearchBuilder.append(splittedUnitedWords);
            resultSearchBuilder.append(andWherePart);
        }
        if (!splittedTags.isEmpty()) {
            resultSearchBuilder.append(tagsWhereCondition);
            resultSearchBuilder.append(splittedTags);
            resultSearchBuilder.append("%'");
        }
        return resultSearchBuilder.isEmpty() ? "" : " WHERE " + resultSearchBuilder;
    }


        public PostDTO addPost(PostDTO post) throws IllegalArgumentException {
            if (post.getTitle() == null || post.getTitle().isEmpty() ||
                    post.getText() == null || post.getText().isEmpty())
                throw new IllegalArgumentException("Отсутствует заголовок и содержимое поста");
            return postsRepository.addPost(post);
        }

        public PostDTO updatePost(PostDTO post) {
            return postsRepository.updatePost(post);
        }

        public void deletePost(Long id) {
            postsRepository.deletePost(id);
        }


        // Likes
        //==============================================
        public Integer addLike(Long postId) {
            return postsRepository.addLike(postId);
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

        public Comment addComment(Comment comment) {
            return postsRepository.addComment(comment);
        }

        public Comment updateComment(Comment comment) {
            return postsRepository.updateComment(comment);
        }

        public void deleteComment(Long id) {
            postsRepository.deleteComment(id);
        }

    }
