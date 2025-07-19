package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.validation.Marker;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingCreateDto {
    @Null(groups = Marker.OnCreate.class, message = "При добавлении бронирования id должен быть null")
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(groups = Marker.OnCreate.class, message = "Дата начала бронирования не может быть null")
    @Future(groups = Marker.OnCreate.class, message = "Дата начала бронирования не может быть в прошлом")
    private LocalDateTime start;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(groups = Marker.OnCreate.class, message = "Дата окончания бронирования не может быть null")
    private LocalDateTime end;

    @NotNull(groups = Marker.OnCreate.class, message = "Id предмета для бронирования не может быть null")
    private Long itemId;

    @AssertTrue(groups = Marker.OnCreate.class, message = "Дата окончания бронирования" +
            " не может быть раньше даты начала")
    public boolean isEndAfterStart() {
        return start == null || end.isAfter(start);
    }
}
