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

import static org.junit.jupiter.api.Assertions.assertThrows;
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
            PostDTO validPost = new PostDTO(1L, "Test Order", "Check ok status", "#test #moduletest", 0);

            // Проверка вызова метода
            doNothing().when(postsRepository).addPost(validPost);

            // Выполнение метода
            postsService.addPost(validPost);

            // Проверка вызовов
            verify(postsRepository, times(1)).addPost(validPost);
        }

          @Test
        void testAddPost_validationFailure() {
            PostDTO invalidPost = new PostDTO(2L, null, "Check ok status", null, -1);

            // Проверка выброса исключения
            assertThrows(IllegalArgumentException.class, () -> postsService.addPost(invalidPost));

            // Убедимся, что save и log не были вызваны
            verify(postsRepository, never()).addPost(invalidPost);
        }
    }
