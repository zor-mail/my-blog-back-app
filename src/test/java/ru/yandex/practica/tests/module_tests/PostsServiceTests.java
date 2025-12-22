package ru.yandex.practica.tests.module_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.practica.models.PostDTO;
import ru.yandex.practica.tests.config.TestsConfiguration;
import ru.yandex.practica.models.Post;
import ru.yandex.practica.repositories.PostsRepository;
import ru.yandex.practica.services.PostsService;

import java.io.InvalidObjectException;

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



    }
