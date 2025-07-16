package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.validation.Marker;

@Data
@AllArgsConstructor
public class UserDto {
    @Null(groups = Marker.OnCreate.class, message = "При создании id пользователя должен быть null")
    private Long id;
    @NotBlank(groups = Marker.OnCreate.class, message = "Имя не может быть пустым")
    private String name;
    @Email(groups = {Marker.OnCreate.class,Marker.OnUpdate.class}, message = "Неверный формат Email")
    @NotBlank(groups = Marker.OnCreate.class, message = "Почта не может быть пустой")
    private String email;
}