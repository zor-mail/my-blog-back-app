package ru.yandex.practica.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practica.config.DataSourceConfiguration;
import ru.yandex.practica.config.TestDataSourceConfiguration;
import ru.yandex.practica.config.WebConfiguration;
import ru.yandex.practica.config.TestsConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.hasSize;

@SpringJUnitConfig(classes = {
        TestDataSourceConfiguration.class,
        WebConfiguration.class
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
                    VALUES (4,'Зимний вид на гору Фудзи','Прекрасный вид открывается с этой небольшой возвышенности на гору Фудзи', 
                            ARRAY['#фудзи', '#закат'], 0)
                """);
        jdbcTemplate.execute("""
                    INSERT INTO myblog.posts (id, title, text, tags, likes_count)
                    VALUES (5,'Зимний дворец','Белые ночи и выставка *100 видов на гору Фудзи*', 
                            ARRAY['#Питер', '#белыеночи'], 0)
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
                .param("search", "зимний #фудзи")
                .param("pageNumber", "1")
                .param("pageSize", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
/*                .andExpect(jsonPath("$.posts[0].title").value("Зимний вид на гору Фудзи"))
                .andExpect(jsonPath("$.posts[1].title").value("Зимний дворец"));*/

        System.out.println("###########  Posts ############");
        System.out.println(result.andReturn().getResponse().getContentAsString());

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

/*        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Мой пост №1"))
                .andExpect(jsonPath("$.text").value("С чего же начать..."));*/

        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated());

        var result = mockMvc.perform(get("/posts")
                        .param("id", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Зимний вид на гору Фудзи"));

/*
        var result = mockMvc.perform(get("/posts")
                .param("search", "зимний #фудзи")
                .param("pageNumber", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk());
*/
                // .andExpect(jsonPath("$", hasSize(3)));
        System.out.println("###########  Create Post ############");
        System.out.println(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void deletePost_noContent() throws Exception {
        mockMvc.perform(delete("/posts/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Зимний вид на гору Фудзи"));

        var result = mockMvc.perform(get("/posts")
                .param("search", "зимний #фудзи")
                .param("pageNumber", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                // .andExpect(jsonPath("$.posts", hasSize(1)))
                .andReturn();
        System.out.println("###########  Delete Post ############");
        System.out.println(result.getResponse().getContentAsString());
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
