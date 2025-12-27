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
import ru.yandex.practica.models.PostDTO;
import ru.yandex.practica.testconfig.TestDataSourceConfiguration;
import ru.yandex.practica.testconfig.TestWebConfiguration;
import ru.yandex.practica.testconfig.TestsConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.hamcrest.Matchers.hasSize;

/*@SpringJUnitConfig(classes = {
        TestDataSourceConfiguration.class,
        TestWebConfiguration.class
})*/
@SpringJUnitConfig(classes = {
        DataSourceConfiguration.class,
        WebConfiguration.class
})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:application.properties")
//@TestPropertySource(locations = "classpath:test-application.properties")

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
        jdbcTemplate.execute("DELETE FROM myblog.posts");
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
                    INSERT INTO myblog.comments (post_id, text)
                    VALUES (4,'Крута гора Фудзи...')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO myblog.comments (post_id, text)
                    VALUES (4,'Круто, Улитка, ползи!')
                """);

        jdbcTemplate.execute("""
                    INSERT INTO myblog.comments (post_id, text)
                    VALUES (5,'Питер крут, Питеру виват!')
                """);
    }

    @Test
    void getPosts_returnsJsonArray() throws Exception {

        var result = mockMvc.perform(get("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .param("search", "зимний #фудзи")
                //.param("search", "зимний")
                .param("pageNumber", "1")
                .param("pageSize", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("Зимний вид на гору Фудзи"))
                //.andExpect(jsonPath("$.posts[1].title").value("Зимний дворец"))
                .andExpect(jsonPath("$.posts[0].tags[0]").value("фудзи"));



        System.out.println("###########  Posts ############");
        String body = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(body);

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


        var result2 = mockMvc.perform(get("/posts/{id}", newPostId)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Мой пост №1"));

        System.out.println("###########  Create Post ############");
        System.out.println(result2.andReturn().getResponse().getContentAsString());
        String body = result2.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(body);
    }

    @Test
    void deletePost_noContent() throws Exception {
        mockMvc.perform(delete("/posts/4"))
                .andExpect(status().isOk());

        var result = mockMvc.perform(get("/posts")
                        .characterEncoding("UTF-8")
                .param("search", "")
                .param("pageNumber", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andReturn();
        System.out.println("###########  Delete Post ############");
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(body);
    }

    @Test
    void getImage_success() throws Exception {
        File image = new File("./src/test/resources/Files/nature.jpg");

        mockMvc.perform(get("/posts/{id}/image", 4L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().bytes(Files.readAllBytes(image.toPath())));
    }


    @Test
    void uploadAndDownloadImage_success() throws Exception {
        File image = new File("./src/test/resources/Files/nature.jpg");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "nature.jpg",
                MediaType.IMAGE_PNG_VALUE,
                Files.readAllBytes(image.toPath())
        );

        mockMvc.perform(multipart("/posts/{id}/image", 4L)
                        .file(multipartFile)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk());

        mockMvc.perform(get("/posts/{id}/image", 4L))
                .andDo(print())
                .andExpect(status().isOk());

/*        mockMvc.perform(get("/posts/{id}/image", 4L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(Files.readAllBytes(image.toPath())));*/

    }

    @Test
    void uploadImage_emptyFile_badRequest() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/posts/{id}/image", 1L).file(empty))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("empty file"));
    }

    @Test
    void uploadImage_userNotFound_404() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "testFile.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/posts/{id}/image", 333L).file(file))
                .andExpect(status().isNotFound())
                .andExpect(content().string("post not found"));
    }
}
