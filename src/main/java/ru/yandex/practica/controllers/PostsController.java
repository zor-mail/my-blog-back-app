package ru.yandex.practica.controllers;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import ru.yandex.practica.models.Post;
import ru.yandex.practica.services.PostsService;

@RestController
@RequestMapping("/users")
public class PostsController {

    private final PostsService postsService;

    public PostsController(PostsService postsService) {
        this.postsService = postsService;
    }

    @GetMapping
    public List<Post> getUsers() {
        return postsService.findAll();
    }

    @PostMapping
    public void save(@RequestBody Post post) {
        postsService.addUser(post);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable(name = "id") Long id, @RequestBody Post post) {
        postsService.update(id, post);
    }

    @DeleteMapping(value = "/{id}")
    public void delete(@PathVariable(name = "id") Long id) {
        postsService.deleteById(id);
    }
}
