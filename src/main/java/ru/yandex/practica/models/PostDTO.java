package ru.yandex.practica.models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {
    Long id;
    String title;
    String text;
    String[] tags;
    Integer likesCount;
    Integer commentsCount;
}
