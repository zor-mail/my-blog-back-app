package ru.yandex.practica.models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
   Long id;
   Long postId;
   String text;
}
