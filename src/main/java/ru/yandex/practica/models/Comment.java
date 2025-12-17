package ru.yandex.practica.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Comment {
   long id;
   long postId;
   String text;
}
