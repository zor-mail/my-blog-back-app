package ru.yandex.practica.models;

import java.util.List;

public record PostsDTO(
        List<Post> posts,
        Boolean hasPrev,
        Boolean hasNext,
        Integer lastPage
        ) {
}
