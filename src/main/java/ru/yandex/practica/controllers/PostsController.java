package ru.yandex.practica.controllers;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.Post;
import ru.yandex.practica.models.PostDTO;
import ru.yandex.practica.models.PostsDTO;
import ru.yandex.practica.services.PostsService;

@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostsService postsService;

    public PostsController(PostsService postsService) {
        this.postsService = postsService;
    }


    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PostDTO getPost(
            @PathVariable(name = "id") Long postId
    ) {
        return postsService.getPost(postId);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PostsDTO getPosts(
            @RequestParam(value = "search", required = true) String search,
            @RequestParam(value = "pageNumber", required = true) Integer pageNumber,
            @RequestParam(value = "pageSize", required = true) Integer pageSize

    ) {
        return postsService.getPosts(search, pageNumber, pageSize);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostDTO addPost(@RequestBody PostDTO post) throws IllegalArgumentException {
        return postsService.addPost(post);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PostDTO updatePost(@PathVariable(name = "id") Long id, @RequestBody PostDTO post) {
        return postsService.updatePost(post);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deletePost(@PathVariable(name = "id") Long id) {
        postsService.deletePost(id);
    }



    // Likes
    //==============================================
    @PostMapping("/{id}/likes")
    @ResponseStatus(HttpStatus.OK)
    public Integer addLike(@PathVariable(name = "id") Long postId) {
        return postsService.addLike(postId);
    }


    // Images
    //==============================================
    @GetMapping(path="/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public byte[] getImage(
            @PathVariable(name = "id") Long postId
    ) {
        return postsService.getImage(postId);
    }

    @PutMapping("/{id}/image")
    @ResponseStatus(HttpStatus.OK)
    public void updateImage(
            @PathVariable(name = "id") Long postId,
            @RequestBody MultipartFile image) throws IOException {
        postsService.updateImage(postId, image);
    }


    // Comments
    //==============================================

    @GetMapping(path="/{id}/comments/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Comment getComment(
            @PathVariable(name = "id") Long postId,
            @PathVariable(name = "commentId") Long commentId
    ) {
        return postsService.getComment(commentId);
    }

    @GetMapping(path="/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<Comment> getComments(
            @PathVariable(name = "id") Long postId
    ) {
        return postsService.getComments(postId);
    }

    @PostMapping(path = "/{id}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Comment addComment(
            @PathVariable(name = "id") Long postId,
            @RequestBody Comment comment) {
        return postsService.addComment(comment);
    }

    @PutMapping(path="/{id}/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Comment updateComment(
            @RequestBody Comment comment) {
        return postsService.updateComment(comment);
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteComment(@PathVariable(name = "id") Long id) {
        postsService.deleteComment(id);
    }
}
