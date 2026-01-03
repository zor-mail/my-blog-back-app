package ru.yandex.practica.controllers;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practica.models.Comment;
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
        int deleted = postsService.deletePost(id);
        if (deleted == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found");
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
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(
            @PathVariable("id") Long postId) {
        byte[] image = postsService.getImage(postId);
        if (image == null) {
            return ResponseEntity.ofNullable(null);
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.noStore())
                .body(image);
    }

    @PutMapping(path="/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateImage(
            @PathVariable("id") Long postId,
            @RequestParam("image") MultipartFile image) throws IOException {
        if (image.isEmpty()) {
            return ResponseEntity.badRequest().body("empty file");
        }
        Integer updatedRows = postsService.updateImage(postId, image);
        if (updatedRows == null || updatedRows == 0)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok().build();
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
    public void deleteComment(@PathVariable(name = "commentId") Long commentId) {
        int deleted = postsService.deleteComment(commentId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "comment not found");
        }
    }
}
