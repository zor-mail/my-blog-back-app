package ru.yandex.practica.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class PostDTO {
    Long id;
    String title;
    String text;
    String tags;
    Integer likesCount;
    Integer commentsCount;
}
