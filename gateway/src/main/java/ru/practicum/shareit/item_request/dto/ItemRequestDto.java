package ru.practicum.shareit.item_request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.validation.Marker;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ItemRequestDto {
    @Null(groups = Marker.OnCreate.class, message = "При добавлении запроса id должен быть null")
    private Long id;

    @NotBlank(groups = Marker.OnCreate.class, message = "Описание запроса не может быть пустым")
    @Size(groups = {Marker.OnCreate.class, Marker.OnUpdate.class}, max = 400,
            message = "Длина описания превышает 400 символов")
    private String description;

    private LocalDateTime created;
}