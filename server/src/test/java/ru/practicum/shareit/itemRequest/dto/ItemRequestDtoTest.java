package ru.practicum.shareit.itemRequest.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoTest {
    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestDtoJacksonTester;

    @Autowired
    private JacksonTester<ItemRequestAllFieldsDto> itemRequestAllFieldsDtoJacksonTester;

    @Test
    void serializeItemRequestDto() throws Exception {
        var itemRequestDto = new ItemRequestDto(
                1L,
                "about",
                LocalDateTime.now()
        );
        var jsonContent = itemRequestDtoJacksonTester.write(itemRequestDto);
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.description")
                .isEqualTo(itemRequestDto.getDescription());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(itemRequestDto.getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.created")
                .isEqualTo(itemRequestDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void serializeAllFieldsDto() throws Exception {
        ItemRequestAllFieldsDto all = new ItemRequestAllFieldsDto(
                3L, "Y", LocalDateTime.now(), List.of()
        );
        var content = itemRequestAllFieldsDtoJacksonTester.write(all);
        assertThat(content).extractingJsonPathNumberValue("@.id").isEqualTo(3);
        assertThat(content).extractingJsonPathArrayValue("@.items").isEmpty();
        assertThat(content)
                .extractingJsonPathStringValue("$.description")
                .isEqualTo(all.getDescription());
        assertThat(content).extractingJsonPathStringValue("@.created")
                .isEqualTo(all.getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}