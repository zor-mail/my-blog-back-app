package ru.yandex.practica.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {
        "ru.yandex.practica.controllers",
        "ru.yandex.practica.services",
        "ru.yandex.practica.repositories",
        "ru.yandex.practica.config"}
)
public class WebConfiguration implements WebMvcConfigurer {


    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

        converters.add(new ByteArrayHttpMessageConverter());

        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));

        MappingJackson2HttpMessageConverter json = new MappingJackson2HttpMessageConverter();
        json.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(json);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost") // вместо allowedOrigins(...)
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

}
