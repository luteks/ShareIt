package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoTest {
    @Autowired
    private JacksonTester<ItemAllFieldsDto> itemAllFieldsDtoJacksonTester;
    @Autowired
    private JacksonTester<CommentDto> commentDtoJacksonTester;
    @Autowired
    private JacksonTester<ItemDto> itemDtoJacksonTester;

    @Test
    void itemDtoTest() throws Exception {
        var itemDto = ItemDto.builder()
                .id(1L)
                .name("Pen")
                .description("Blue pen")
                .available(true)
                .requestId(1L)
                .build();

        var jsonContent = itemDtoJacksonTester.write(itemDto);
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.description")
                .isEqualTo(itemDto.getDescription());
        assertThat(jsonContent)
                .extractingJsonPathBooleanValue("$.available")
                .isEqualTo(itemDto.getAvailable());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.requestId")
                .isEqualTo(itemDto.getRequestId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.name")
                .isEqualTo(itemDto.getName());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(itemDto.getId().intValue());
    }

    @Test
    void itemAllFieldsDtoTest() throws Exception {
        var itemAllFieldsDto = ItemAllFieldsDto.builder()
                .id(1L)
                .name("Pen")
                .description("Blue pen")
                .available(true)
                .lastBooking(BookingDto.builder()
                        .id(1L)
                        .start(LocalDateTime.now())
                        .end(LocalDateTime.now().plusDays(1))
                        .item(Item.builder()
                                .id(3L)
                                .name("name")
                                .build())
                        .booker(User.builder()
                                .id(1L)
                                .build())
                        .status(BookingStatus.WAITING)
                        .build())
                .nextBooking(null)
                .comments(null)
                .build();
        var jsonContent = itemAllFieldsDtoJacksonTester.write(itemAllFieldsDto);
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.lastBooking.booker.id")
                .isEqualTo(itemAllFieldsDto.getLastBooking().getBooker().getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.lastBooking.id")
                .isEqualTo(itemAllFieldsDto.getLastBooking().getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.description")
                .isEqualTo(itemAllFieldsDto.getDescription());
        assertThat(jsonContent)
                .extractingJsonPathBooleanValue("$.available")
                .isEqualTo(itemAllFieldsDto.getAvailable());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.name")
                .isEqualTo(itemAllFieldsDto.getName());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(itemAllFieldsDto.getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathArrayValue("$.comments")
                .isNullOrEmpty();
        assertThat(jsonContent)
                .extractingJsonPathMapValue("$.lastBooking")
                .isNotNull();
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.requestId")
                .isNull();
        assertThat(jsonContent)
                .extractingJsonPathValue("$.nextBooking")
                .isNull();
    }

    @Test
    void commentDtoTest() throws Exception {
        var commentDto = CommentDto.builder()
                .id(1L)
                .text("My comment")
                .authorName("Norris")
                .created(LocalDateTime.now())
                .build();
        var jsonContent = commentDtoJacksonTester.write(commentDto);
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.authorName")
                .isEqualTo(commentDto.getAuthorName());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.text")
                .isEqualTo(commentDto.getText());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(commentDto.getId().intValue());
    }
}