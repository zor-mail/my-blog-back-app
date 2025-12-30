package ru.yandex.practica.testconfig;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan(basePackages = {
        "ru.yandex.practica.testconfig",
        "ru.yandex.practica.controllers",
        "ru.yandex.practica.models",
        "ru.yandex.practica.services",
        "ru.yandex.practica.repositories"
})
public class TestWebConfiguration {
}
