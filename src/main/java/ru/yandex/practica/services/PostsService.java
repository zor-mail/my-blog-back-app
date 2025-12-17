package ru.yandex.practica.services;

import org.springframework.stereotype.Service;
import ru.yandex.practica.models.Post;
import ru.yandex.practica.repositories.PostsRepository;

import java.util.List;


    @Service
    public class PostsService {

        private final PostsRepository postsRepository;

        public PostsService(PostsRepository postsRepository) {
            this.postsRepository = postsRepository;
        }

        public List<Post> findAll() {
            return postsRepository.findAll();
        }

        public void addPost(Post post) {
            postsRepository.save(post);
        }

        public void update(Long id, Post post) {
            postsRepository.update(id, post);
        }

        public void deleteById(Long id) {
            postsRepository.deleteById(id);
        }


        public Boolean exists(Long id) {
            return postsRepository.exists(id);
        }
    }
