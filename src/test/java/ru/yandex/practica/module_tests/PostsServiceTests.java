package ru.yandex.practica.module_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.PostDTO;
import ru.yandex.practica.testconfig.TestsConfiguration;
import ru.yandex.practica.repositories.PostsRepository;
import ru.yandex.practica.services.PostsService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestsConfiguration.class)
public class PostsServiceTests {

        @Autowired
        private PostsRepository postsRepository;

        @Autowired
        private PostsService postsService;

        @BeforeEach
        void resetMocks() {
            reset(postsRepository);
        }

      @Test
      void testAddPost_success() throws IllegalArgumentException {
        String[] tagsArray = new String[]{"#test", "#moduletest"};
        PostDTO validPost = new PostDTO(null, "Test Order", "Check ok status", tagsArray, 0, 0);
        PostDTO validSaved = new PostDTO(1L, "Test Order", "Check ok status", tagsArray, 0, 0);

        when(postsRepository.addPost(validPost)).thenReturn(validSaved);
        PostDTO result = postsService.addPost(validPost);

        assertNotNull(result);
        assertEquals(result.getText(), validPost.getText());
        assertEquals(result.getTitle(), validPost.getTitle());

        verify(postsRepository, times(1)).addPost(validPost);
     }

      @Test
      void testAddPost_validationFailure() {
        PostDTO invalidPost = new PostDTO(2L, null, "Check ok status", null, -1, 0);

        // Проверка выброса исключения
        assertThrows(IllegalArgumentException.class, () -> postsService.addPost(invalidPost));

        // Метод не был вызван
        verify(postsRepository, never()).addPost(invalidPost);
     }

    @Test
    void testAddComment_success() throws IllegalArgumentException {

        Comment validComment = new Comment(null, 4L, "Test Order");
        Comment validSaved = new Comment(1L, 4L, "Test Order");

        when(postsRepository.addComment(validComment)).thenReturn(validSaved);
        Comment result = postsService.addComment(validComment);

        assertNotNull(result);
        assertEquals(result.getText(), validComment.getText());

        verify(postsRepository, times(1)).addComment(validComment);
    }

    @Test
    void testAddComment_validationFailure() {
        Comment invalidComment = new Comment(2L, 44L, null);

        // Проверка выброса исключения
        assertThrows(IllegalArgumentException.class, () -> postsService.addComment(invalidComment));

        // Метод не был вызван
        verify(postsRepository, never()).addComment(invalidComment);
    }

    @ParameterizedTest
    @MethodSource("provideStrings")
    void testGetTagsAndWords_splitAndUniteSuccess(String searchString, String expectedWhereString) {

        String tagsAndWords =  postsService.getTagsAndWordsSearchString(searchString, "title", "tags");
        assertEquals(expectedWhereString, tagsAndWords);
    }

    static Stream<Object[]> provideStrings() {
        return Stream.of(
        new Object[]{"река Хопер #река #хопер  природа Волгоградской области #природа ## #",
                " WHERE lower(title) like '%река хопер%' AND lower(title) like '%природа волгоградской области%' " +
                "AND lower(tags) like '%река%' AND lower(tags) like '%хопер%' " +
                "AND lower(tags) like '%природа%'"},
        new Object[]{"", ""},
        new Object[]{"#природа # природа",
                " WHERE lower(title) like '%природа%' AND lower(tags) like '%природа%'"}
        );
    }

}
