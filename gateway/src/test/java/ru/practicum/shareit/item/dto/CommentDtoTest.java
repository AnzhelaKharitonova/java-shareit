package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testSerialize() throws IOException {
        // Given
        LocalDateTime createdTime = LocalDateTime.of(2026, 10, 1, 16, 0, 0);
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Отличный инструмент, всё работает!")
                .authorName("Иван Петров")
                .itemId(10L)
                .created(createdTime)
                .build();

        JsonContent<CommentDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        assertThat(result).hasJsonPathStringValue("$.text");
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Отличный инструмент, всё работает!");

        assertThat(result).hasJsonPathStringValue("$.authorName");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Иван Петров");

        assertThat(result).hasJsonPathNumberValue("$.itemId");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);

        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).extractingJsonPathStringValue("$.created").startsWith("2026-10-01T16:00:00");
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\n" +
                "  \"id\": 2,\n" +
                "  \"text\": \"Инструмент немного изношен\",\n" +
                "  \"authorName\": \"Анна Сидорова\",\n" +
                "  \"itemId\": 15,\n" +
                "  \"created\": \"2026-10-01T16:30:00\"\n" +
                "}";

        CommentDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getText()).isEqualTo("Инструмент немного изношен");
        assertThat(dto.getAuthorName()).isEqualTo("Анна Сидорова");
        assertThat(dto.getItemId()).isEqualTo(15L);
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 10, 1, 16, 30, 0));
    }
}