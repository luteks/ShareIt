package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.validation.Marker;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentDto {
    @Null(groups = Marker.OnCreate.class, message = "При добавлении вещи id должен быть null")
    private Long id;

    @NotBlank(groups = Marker.OnCreate.class, message = "Отзыв не может быть пустым")
    private String text;

    private String authorName;

    private LocalDateTime created;
}