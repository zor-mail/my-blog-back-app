package ru.yandex.practica.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class Post {
    Long id;
    String title;
    String text;
    String tags;
    Integer likesCount;
}
