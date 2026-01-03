package ru.yandex.practica.testconfig;

import org.mockito.Mockito;
import org.springframework.context.annotation.*;
import ru.yandex.practica.repositories.PostsRepository;
import ru.yandex.practica.services.PostsService;

public class TestsConfiguration {

    @Bean
    @Primary
    public PostsRepository mockPostsRepository() {
        return Mockito.mock(PostsRepository.class);
    }

    @Bean
    public PostsService postsService(PostsRepository postsRepository) {
        return new PostsService(postsRepository);
    }
}
