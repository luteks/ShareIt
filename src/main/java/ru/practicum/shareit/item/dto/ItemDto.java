package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.validation.Marker;

@Data
@AllArgsConstructor
public class ItemDto {
    @Null(groups = Marker.OnCreate.class, message = "При добавлении вещи id должен быть null")
    private Long id;
    @NotBlank(groups = Marker.OnCreate.class, message = "Название вещи не долнжо быть пустым")
    private String name;
    @Size(groups = {Marker.OnCreate.class, Marker.OnUpdate.class}, max = 200,
            message = "Длина описания превышает 200 символов")
    @NotNull(groups = Marker.OnCreate.class, message = "Описание фильма не может быть null")
    private String description;
    @NotNull(groups = Marker.OnCreate.class, message = "Доступность вещи не может быть null")
    private Boolean available;
}
