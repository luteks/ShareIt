package ru.practicum.shareit.user.dto;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoTest {
    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void userDtoTest() throws Exception {
        UserDto userDto = new UserDto(
                1L,
                "John",
                "john@mail.com");

        var jsonContent = json.write(userDto);
        assertThat(jsonContent).extractingJsonPathStringValue("$.email")
                .isEqualTo(userDto.getEmail());
        assertThat(jsonContent).extractingJsonPathStringValue("$.name")
                .isEqualTo(userDto.getName());
        assertThat(jsonContent).extractingJsonPathNumberValue("$.id")
                .isEqualTo(userDto.getId().intValue());
    }

    @Test
    void testDeserializeUserDto() throws Exception {
        String content = "{\n" +
                "  \"name\": \"John\",\n" +
                "  \"email\": \"john@example.com\"\n" +
                "}";

        UserDto parsed = json.parseObject(content);

        assertThat(parsed.getName()).isEqualTo("John");
        assertThat(parsed.getEmail()).isEqualTo("john@example.com");
    }
}