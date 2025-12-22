package ru.yandex.practica.tests.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practica.config.DataSourceConfiguration;
import ru.yandex.practica.config.WebConfiguration;
import ru.yandex.practica.tests.config.TestDataSourceConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.hasSize;

@SpringJUnitConfig(classes = {
       TestDataSourceConfiguration.class,
        WebConfiguration.class,
})
@WebAppConfiguration
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
        jdbcTemplate.execute("DELETE FROM myblog.posts");
        jdbcTemplate.execute("""
                    INSERT INTO myblog.posts (id, title, text, tags, likes_count)
                    VALUES (4,'Вид на гору Фудзи','Прекрасный вид открывается с этой небольшой возвышенности на гору Фудзи', 
                            ARRAY['#фудзи', '#закат'], 0)
                """);
        jdbcTemplate.execute("""
                    INSERT INTO myblog.posts (id, title, text, tags, likes_count)
                    VALUES (5,'Зимний дворец','Белые ночи и Зимний дворец над Невой - мечта туриста', 
                            ARRAY['#Питер', '#белыеночи'], 0)
                """);


        jdbcTemplate.execute("""
                    INSERT INTO myblog.comments (post_id, text)
                    VALUES (4,'Круто...')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO myblog.comments (post_id, text)
                    VALUES (4,'Улитка, ползи')
                """);

        jdbcTemplate.execute("""
                    INSERT INTO myblog.comments (post_id, text)
                    VALUES (5,'Питеру виват!')
                """);
    }

    @Test
    void getPosts_returnsJsonArray() throws Exception {
        mockMvc.perform(get("/posts")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Вид на гору Фудзи"))
                .andExpect(jsonPath("$[1].title").value("Зимний дворец"));
    }

    @Test
    void createPost_acceptsJson_andPersists() throws Exception {
        String json = """
                   {
                      "title": "Мой пост №1",
                      "text": "С чего же начать...",
                      "tags": ["#былое", "#думы"]
                    }
                """;

        mockMvc.perform(post("/posts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Мой пост №1"))
                .andExpect(jsonPath("$.text").value("С чего же начать..."));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void deletePost_noContent() throws Exception {
        mockMvc.perform(delete("/posts/4"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

  /*  @Test
    void uploadAndDownloadAvatar_success() throws Exception {
        byte[] pngStub = new byte[]{(byte) 137, 80, 78, 71};
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", pngStub);

        mockMvc.perform(multipart("/api/users/{id}/avatar", 1L).file(file))
                .andExpect(status().isCreated())
                .andExpect(content().string("ok"));

        mockMvc.perform(get("/api/users/{id}/avatar", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().bytes(pngStub));
    }

    @Test
    void uploadAvatar_emptyFile_badRequest() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        mockMvc.perform(multipart("/api/users/{id}/avatar", 1L).file(empty))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("empty file"));
    }

    @Test
    void uploadAvatar_userNotFound_404() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/users/{id}/avatar", 999L).file(file))
                .andExpect(status().isNotFound())
                .andExpect(content().string("user not found"));
    }

    @Test
    void getAvatar_userHasNoAvatar_404() throws Exception {
        mockMvc.perform(get("/api/users/{id}/avatar", 2L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAvatar_userNotFound_404() throws Exception {
        mockMvc.perform(get("/api/users/{id}/avatar", 777L))
                .andExpect(status().isNotFound());
    }*/
}
