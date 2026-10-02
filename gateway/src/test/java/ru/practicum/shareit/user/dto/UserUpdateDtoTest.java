package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserUpdateDtoTest {

    @Autowired
    private JacksonTester<UserUpdateDto> json;

    @Test
    void testSerialize() throws IOException {
        // Given
        UserUpdateDto dto = UserUpdateDto.builder()
                .id(1L)
                .name("Иван Измененный")
                .email("ivan_new@example.com")
                .build();

        // When
        JsonContent<UserUpdateDto> result = json.write(dto);

        // Then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Иван Измененный");

        assertThat(result).hasJsonPathStringValue("$.email");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("ivan_new@example.com");
    }

    @Test
    void testDeserializeAllFields() throws IOException {
        // Given
        String jsonContent = "{\n" +
                "  \"id\": 10,\n" +
                "  \"name\": \"Петр\",\n" +
                "  \"email\": \"petr@example.com\"\n" +
                "}";

        // When
        UserUpdateDto dto = json.parse(jsonContent).getObject();

        // Then
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("Петр");
        assertThat(dto.getEmail()).isEqualTo("petr@example.com");
    }

    @Test
    void testDeserializePartialFields() throws IOException {
        // Given: при обновлении передается только email
        String jsonContent = "{\n" +
                "  \"email\": \"only_email@example.com\"\n" +
                "}";

        // When
        UserUpdateDto dto = json.parse(jsonContent).getObject();

        // Then
        assertThat(dto.getEmail()).isEqualTo("only_email@example.com");
        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isNull();
    }
}