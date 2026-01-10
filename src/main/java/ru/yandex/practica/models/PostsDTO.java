package ru.yandex.practica.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostsDTO {
    List<PostDTO> posts;
    Boolean hasPrev;
    Boolean hasNext;
    Integer lastPage;
}
