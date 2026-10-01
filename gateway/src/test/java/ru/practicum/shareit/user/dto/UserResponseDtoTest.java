package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserResponseDtoTest {

    @Autowired
    private JacksonTester<UserResponseDto> json;

    @Test
    void testSerialize() throws IOException {
        // Given
        UserResponseDto dto = UserResponseDto.builder()
                .id(1L)
                .name("Иван Петров")
                .email("ivan@example.com")
                .build();

        // When
        JsonContent<UserResponseDto> result = json.write(dto);

        // Then
        // Проверяем наличие полей в JSON и соответствие их типов/значений контракту API
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Иван Петров");

        assertThat(result).hasJsonPathStringValue("$.email");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("ivan@example.com");
    }
}