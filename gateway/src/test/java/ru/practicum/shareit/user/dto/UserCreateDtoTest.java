package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserCreateDtoTest {

    @Autowired
    private JacksonTester<UserCreateDto> json;

    @Test
    void testSerialize() throws IOException {
        // Given
        UserCreateDto dto = UserCreateDto.builder()
                .name("Алексей")
                .email("alex@example.com")
                .build();

        // When
        JsonContent<UserCreateDto> result = json.write(dto);

        // Then
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Алексей");

        assertThat(result).hasJsonPathStringValue("$.email");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("alex@example.com");
    }

    @Test
    void testDeserialize() throws IOException {
        // Given
        String jsonContent = "{\n" +
                "  \"name\": \"Мария\",\n" +
                "  \"email\": \"maria@example.com\"\n" +
                "}";

        // When
        UserCreateDto dto = json.parse(jsonContent).getObject();

        // Then
        assertThat(dto.getName()).isEqualTo("Мария");
        assertThat(dto.getEmail()).isEqualTo("maria@example.com");
    }
}