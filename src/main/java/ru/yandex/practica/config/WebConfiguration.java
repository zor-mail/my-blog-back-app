package ru.yandex.practica.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.yandex.practica.services.PostsService;

@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = "ru.yandex.practica",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = DataSourceConfiguration.class
        )
)
public class WebConfiguration {
}
