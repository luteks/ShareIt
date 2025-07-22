package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoTest {
    private final ItemDto itemDto = new ItemDto(1L, "Pen", "Blue pen",
            true, 4L);
    private final LocalDateTime startTime = LocalDateTime.of(2025, 6, 11, 10, 0);
    private final LocalDateTime endTime = startTime.plusDays(1);
    private final UserDto userDto = new UserDto(1L, "Lora", "lora@mail.com");
    @Autowired
    private JacksonTester<BookingDto> bookingDtoJacksonTester;
    @Autowired
    private JacksonTester<BookingCreateDto> bookingCreateDtoJacksonTester;

    private final BookingDto bookingDto = BookingDto.builder()
            .id(1L)
            .start(startTime)
            .end(endTime)
            .item(Item.builder()
                    .id(itemDto.getId())
                    .name(itemDto.getName())
                    .build())
            .booker(User.builder()
                    .id(userDto.getId())
                    .build())
            .status(BookingStatus.WAITING)
            .build();

    private final BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
            .id(1L)
            .start(startTime)
            .end(endTime)
            .itemId(1L)
            .build();

    @Test
    void bookingCreateDtoJacksonTesterTest() throws Exception {
        var jsonContent = bookingCreateDtoJacksonTester.write(bookingCreateDto);
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(bookingCreateDto.getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.start")
                .isEqualTo(bookingCreateDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.end")
                .isEqualTo(bookingCreateDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(bookingCreateDto.getItemId().intValue());
    }

    @Test
    void bookingDtoJacksonTesterTest() throws Exception {
        var jsonContent = bookingDtoJacksonTester.write(bookingDto);
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(bookingDto.getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.start")
                .isEqualTo(bookingDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.end")
                .isEqualTo(bookingDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(jsonContent)
                .extractingJsonPathMapValue("$.item").isNotNull();
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.item.id")
                .isEqualTo(bookingDto.getItem().getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.item.name")
                .isEqualTo(bookingDto.getItem().getName());
        assertThat(jsonContent)
                .extractingJsonPathMapValue("$.booker")
                .isNotNull();
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.booker.id")
                .isEqualTo(bookingDto.getBooker().getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.status")
                .isEqualTo(bookingDto.getStatus().toString());
    }
}