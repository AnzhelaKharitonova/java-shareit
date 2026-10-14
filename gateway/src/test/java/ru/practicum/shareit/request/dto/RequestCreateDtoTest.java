package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class RequestCreateDtoTest {

    @Autowired
    private JacksonTester<RequestCreateDto> json;

    @Test
    void testSerialize() throws IOException {
        // Given
        RequestCreateDto dto = RequestCreateDto.builder()
                .description("Нужна стремянка на выходные")
                .build();

        // When
        JsonContent<RequestCreateDto> result = json.write(dto);

        // Then
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна стремянка на выходные");
    }

    @Test
    void testDeserialize() throws IOException {
        // Given
        String jsonContent = "{\n" +
                "  \"description\": \"Ищу перфоратор в аренду\"\n" +
                "}";

        // When
        RequestCreateDto dto = json.parse(jsonContent).getObject();

        // Then
        assertThat(dto.getDescription()).isEqualTo("Ищу перфоратор в аренду");
    }
}