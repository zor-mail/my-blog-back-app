package ru.yandex.practica.testconfig;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import ru.yandex.practica.repositories.PostsRepository;
import ru.yandex.practica.services.PostsService;
@TestConfiguration
public class TestsConfiguration {

    @Bean
    @Primary
    public PostsRepository mockPostsRepository() {
        return Mockito.mock(PostsRepository.class);
    }
}
