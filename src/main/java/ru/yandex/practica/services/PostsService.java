package ru.yandex.practica.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.PostDTO;
import ru.yandex.practica.models.PostsDTO;
import ru.yandex.practica.repositories.PostsRepository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
            String postSearchSQL =
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
                            " ON ps.id = comm.post_id";

            String postSearchCountSQL =
                    "SELECT count(*) as counter FROM myblog.posts";

            Map<String, List<String>> sqlWithParams = getTagsAndWordsSearchQueryString(postSearchCountSQL, searchString,
                    "title", "tags", false);

            Long recordsCount = postsRepository.getRecordsCount(sqlWithParams);


            sqlWithParams = getTagsAndWordsSearchQueryString(postSearchSQL, searchString,
                    "title", "tags", true);

            int lastPage = (int)Math.ceil((double) recordsCount / pageSize);
            boolean hasPrev = pageNumber != 1;
            boolean hasNext = pageNumber < lastPage;
            long offset = (long) pageSize * (pageNumber - 1);

            if (recordsCount == 0 || offset >= recordsCount)
                return new PostsDTO(new ArrayList<PostDTO>(), hasPrev, hasNext, lastPage);

            List<PostDTO> posts = postsRepository.getPosts(sqlWithParams, pageSize, offset);
            return new PostsDTO(posts, hasPrev, hasNext, lastPage);
        }

    public Map<String, List<String>> getTagsAndWordsSearchQueryString(String querySQL, String searchString,
                                         String titleColumnName, String tagsColumnName, boolean withLimitAndOffset) {

        Map<String, List<String>> sqlWithParamsMap = new HashMap<>();
        List<String> queryParams = new ArrayList<>();
        StringBuilder whereConditionBuilder = new StringBuilder();
        String limitOffset = " LIMIT ? OFFSET ?";

        if (searchString == null || searchString.trim().isEmpty()) {
            if (withLimitAndOffset)
                querySQL += limitOffset;
            sqlWithParamsMap.put(querySQL, queryParams);
            return sqlWithParamsMap;
        }
        String titleWhereCondition = String.format("lower(%s) like ?", titleColumnName);
        String tagsWhereCondition = String.format("lower(%s) like ?", tagsColumnName);
        List<String> splittedUnitedWords = new ArrayList<>();

        List<String> splittedTags = Arrays.stream(searchString.split("\\s+")).
                filter(substr -> substr.startsWith("#") && substr.length() > 1).
                map(substr -> substr.substring(1)).
                filter(substr -> !substr.matches("#+")).
                map(substr -> "%" + substr + "%").
                toList();
        if (splittedTags.isEmpty())
            splittedUnitedWords.add(searchString);
        else {
            whereConditionBuilder.append(splittedTags.stream().map(tag -> tagsWhereCondition).
                    collect(Collectors.joining(" AND ")));
            // убирается первое слово в каждом подмассиве, начинающееся с # (как тэг)
            splittedUnitedWords = Arrays.stream(("for_del " + searchString).split("#")).map(substr -> {
                        String[] substrArr = substr.split("\\s+");// для пробелов, табов и т.п.
                        if (substrArr.length < 2)
                            return null;
                        return String.join(" ", Arrays.copyOfRange(substrArr, 1, substrArr.length));
                    }).filter(substr -> substr != null && !substr.trim().isEmpty()).
                    map(String::toLowerCase).
                    collect(Collectors.toList());
            if (!splittedUnitedWords.isEmpty())
                whereConditionBuilder.append(" AND ");
        }
        if (!splittedUnitedWords.isEmpty()) {
            splittedUnitedWords = splittedUnitedWords.stream().map(substr -> "%" + substr + "%").collect(Collectors.toList());
            whereConditionBuilder.append(splittedUnitedWords.stream().map(substr -> titleWhereCondition).
                    collect(Collectors.joining(" AND ")));
        }
        queryParams = Stream.of(splittedTags, splittedUnitedWords)
                .flatMap(List::stream).collect(Collectors.toList());                ;

        if (!splittedUnitedWords.isEmpty() || !splittedTags.isEmpty()) {
            whereConditionBuilder.insert(0, " WHERE ");
            querySQL += whereConditionBuilder;
        }
        if (withLimitAndOffset)
            querySQL += limitOffset;

        sqlWithParamsMap.put(querySQL, queryParams);
        return sqlWithParamsMap;
    }


        public PostDTO addPost(PostDTO post) throws IllegalArgumentException {
            if (post.getTitle() == null || post.getTitle().isEmpty() ||
                    post.getText() == null || post.getText().isEmpty() ||
                    post.getTags() == null || post.getTags().length == 0)
                throw new IllegalArgumentException("Отсутствует заголовок, содержимое поста или тэги поста");
            return postsRepository.addPost(post);
        }

        public PostDTO updatePost(PostDTO post) {
            return postsRepository.updatePost(post);
        }

        public Integer deletePost(Long id) {
            return postsRepository.deletePost(id);
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

        public Integer updateImage(Long postId, MultipartFile image) throws IOException {
            String fileName = image.getOriginalFilename();
            return postsRepository.updateImage(postId, fileName, image.getBytes());
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
            if (comment.getText() == null || comment.getText().isBlank())
                throw new IllegalArgumentException("Отсутствует содержимое комментария");
            return postsRepository.addComment(comment);
        }

        public Comment updateComment(Comment comment) {
            return postsRepository.updateComment(comment);
        }

        public Integer deleteComment(Long id) {
            return postsRepository.deleteComment(id);
        }

    }
