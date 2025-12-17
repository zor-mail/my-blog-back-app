package ru.yandex.practica.controllers;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.Post;
import ru.yandex.practica.services.PostsService;

@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostsService postsService;

    public PostsController(PostsService postsService) {
        this.postsService = postsService;
    }


    @GetMapping("/{id}")
    public Post getPost(
            @PathVariable(name = "id") Long postId
    ) {
        return postsService.getPost(postId);
    }

    @GetMapping
    public List<Post> getPosts(
            @RequestParam("search") String search,
            @RequestParam("pageNumber") Integer pageNumber,
            @RequestParam("pageSize") Integer pageSize

    ) {
        return postsService.getPosts(search, pageNumber, pageSize);
    }

    @PostMapping
    public void addPost(@RequestBody Post post) {
        postsService.addPost(post);
    }

    @PutMapping("/{id}")
    public void updatePost(@PathVariable(name = "id") Long id, @RequestBody Post post) {
        postsService.updatePost(id, post);
    }

    @DeleteMapping(value = "/{id}")
    public void deletePost(@PathVariable(name = "id") Long id) {
        postsService.deletePost(id);
    }



    // Likes
    //==============================================
    @PostMapping("/{id}/likes")
    public void addLike(@RequestBody Post post) {
        postsService.addPost(post);
    }


    // Images
    //==============================================
    @GetMapping("/{id}/image")
    public Post getImage(
            @PathVariable(name = "id") Long postId
    ) {
        return postsService.getImage(postId);
    }

    @PutMapping("/{id}/image")
    public void updateImage(@PathVariable(name = "id") Long postId, @RequestBody MultipartFile image) {
        postsService.updateImage(postId, image);
    }



    // Comments
    //==============================================

    @GetMapping("/{id}/comments/{commentId}")
    public Comment getComment(
            @PathVariable(name = "id") Long postId,
            @PathVariable(name = "commentId") Long commentId
    ) {
        return postsService.getComment(commentId);
    }

    @GetMapping("/{id}/comments")
    public List<Comment> getComments(
            @PathVariable(name = "id") Long postId
    ) {
        return postsService.getComments(postId);
    }

    @PostMapping("/{id}/comments")
    public void addComment(
            @PathVariable(name = "id") Long postId,
            @RequestBody Comment comment) {
        postsService.addComment(postId, comment);
    }

    @PutMapping("/{id}/comments/{commentId}")
    public void updateComment(
            @PathVariable(name = "id") Long postId,
            @PathVariable(name = "commentId") Long commentId,
            @RequestBody Comment comment) {
        postsService.updateComment(commentId, comment);
    }

    @DeleteMapping(value = "/{id}")
    public void deleteComment(@PathVariable(name = "id") Long id) {
        postsService.deleteComment(id);
    }
}
