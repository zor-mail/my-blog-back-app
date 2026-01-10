package ru.yandex.practica.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practica.config.DataSourceConfiguration;
import ru.yandex.practica.config.WebConfiguration;
import ru.yandex.practica.models.Comment;
import ru.yandex.practica.models.PostDTO;
import ru.yandex.practica.testconfig.TestDataSourceConfiguration;
import ru.yandex.practica.testconfig.TestWebConfiguration;

import java.io.File;
import java.nio.file.Files;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@SpringJUnitConfig(classes = {
        TestDataSourceConfiguration.class,
        TestWebConfiguration.class
})
/*@SpringJUnitConfig(classes = {
        DataSourceConfiguration.class,
        WebConfiguration.class
})*/
@WebAppConfiguration
//@TestPropertySource(locations = "classpath:application.properties")
@TestPropertySource(locations = "classpath:test-application.properties")

class PostsControllerIntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        // Чистим и наполняем БД перед каждым тестом
        jdbcTemplate.execute("DELETE FROM myblog.posts WHERE id = 4 OR id = 5");
        jdbcTemplate.execute("""
                    INSERT INTO myblog.posts (id, title, text, tags, likes_count)
                    VALUES (4,'Зимний вид на гору Фудзи','Прекрасный вид открывается с этой небольшой возвышенности на гору Фудзи', 
                            'фудзи, закат', 0)
                """);
        jdbcTemplate.execute("""
                    INSERT INTO myblog.posts (id, title, text, tags, likes_count)
                    VALUES (5,'Зимний дворец','Белые ночи и выставка *100 видов на гору Фудзи*', 
                            'Питер, белыеночи', 0)
                """);

        jdbcTemplate.execute("""
                    INSERT INTO myblog.comments (id, post_id, text)
                    VALUES (400, 4,'Крута гора Фудзи...')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO myblog.comments (id, post_id, text)
                    VALUES (401, 4,'Круто, Улитка, ползи!')
                """);

        jdbcTemplate.execute("""
                    INSERT INTO myblog.comments (id, post_id, text)
                    VALUES (402, 5,'Питер крут, Питеру виват!')
                """);
    }

    @Test
    void getPosts_returnsJsonArray() throws Exception {

        var result = mockMvc.perform(get("/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .param("search", "фудзи")
                    .param("pageNumber", "1")
                    .param("pageSize", "5")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("Зимний вид на гору Фудзи"))
                .andExpect(jsonPath("$.posts[0].tags[0]").value("фудзи"));
    }

    @Test
    void getPosts_returnsEmptyArray() throws Exception {

        mockMvc.perform(get("/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .param("search", "lsksd;als")
                    .param("pageNumber", "1")
                    .param("pageSize", "5")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(0)));
    }

    @Test
    void createPost_acceptsJson_andPersists() throws Exception {
        String json = """
                   {
                      "title": "Мой пост №1",
                      "text": "С чего начать...",
                      "tags": ["#былое", "#думы"]
                    }
                """;
        MvcResult result = mockMvc.perform(post("/posts")
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Мой пост №1"))
                .andExpect(jsonPath("$.text").value("С чего начать..."))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        PostDTO postObject = mapper.readValue(responseJson, PostDTO.class);
        Assertions.assertNotNull(postObject);
        Long newPostId = postObject.getId();

        mockMvc.perform(get("/posts/{id}", newPostId)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Мой пост №1"));
    }

    @Test
    void deletePost_success() throws Exception {
        mockMvc.perform(delete("/posts/{id}", 4))
                .andExpect(status().isOk());

        mockMvc.perform(get("/posts")
                        .characterEncoding("UTF-8")
                        .param("search", "зимний")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andReturn();
    }

    @Test
    void deletePost_notFound() throws Exception {
        mockMvc.perform(delete("/posts/{id}", 44))
                .andExpect(status().isNotFound());

        var result = mockMvc.perform(get("/posts")
                        .characterEncoding("UTF-8")
                        .param("search", "зимний")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(2)))
                .andReturn();
    }

    @Test
    void uploadAndDownloadImage_success() throws Exception {
        File image = new File("./src/test/resources/Files/nature.jpg");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "nature.jpg",
                MediaType.IMAGE_PNG_VALUE,
                Files.readAllBytes(image.toPath())
        );

        mockMvc.perform(multipart("/posts/{id}/image", 4L)
                        .file(multipartFile)
                        .with(request -> { request.setMethod("PUT"); return request; })
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/posts/{id}/image", 4L)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().bytes(Files.readAllBytes(image.toPath())));
    }

    @Test
    void uploadImage_emptyFile_badRequest() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("image", "empty.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/posts/{id}/image", 1L).
                        file(empty)
                .with(req -> { req.setMethod("PUT"); return req; })
                .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("empty file"));
    }

    @Test
    void uploadImage_imageNotFound_404() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "testFile.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/posts/{id}/image", 44L)
                .file(file)
                .with(req -> { req.setMethod("PUT"); return req; })
                .characterEncoding("UTF-8"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addLike_returnsLikeCountPlusOne() throws Exception {

        mockMvc.perform(post("/posts/{id}/likes", 4L)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void getComment_returnsJson() throws Exception {

        mockMvc.perform(get("/posts/{id}/comments/{commentId}", 4L, 401L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Круто, Улитка, ползи!"));
    }

    @Test
    void getComments_returnsJsonArray() throws Exception {

        mockMvc.perform(get("/posts/{id}/comments", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Крута гора Фудзи..."));
    }

    @Test
    void createComment_acceptsJson_andPersists() throws Exception {
        String json = """
                      {
                        "text": "Белые ночи",
                        "postId": 4
                      }
                """;
        MvcResult result = mockMvc.perform(post("/posts/{id}/comments", 5)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value("Белые ночи"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        Comment commentObject = mapper.readValue(responseJson, Comment.class);
        Assertions.assertNotNull(commentObject);

        Long newCommentId = commentObject.getId();


        mockMvc.perform(get("/posts/{id}/comments/{commentId}", 5, newCommentId)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Белые ночи"));
    }

    @Test
    void deleteComment_success() throws Exception {
        mockMvc.perform(delete("/posts/{id}/comments/{commentId}", 4L, 400))
                .andExpect(status().isOk());

        mockMvc.perform(get("/posts/{id}/comments", 4L)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();
    }

    @Test
    void deleteComment_notFound() throws Exception {
        mockMvc.perform(delete("/posts/{id}/comments/{commentId}", 4L, 405))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/posts/{id}/comments", 4L)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();
    }
}
