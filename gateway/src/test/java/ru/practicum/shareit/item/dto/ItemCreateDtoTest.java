package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemCreateDtoTest {

    @Autowired
    private JacksonTester<ItemCreateDto> json;

    @Test
    void testSerialize() throws IOException {
        ItemCreateDto dto = ItemCreateDto.builder()
                .name("Дрель")
                .description("Ударная дрель Bosch")
                .available(true)
                .requestId(42L)
                .build();

        JsonContent<ItemCreateDto> result = json.write(dto);

        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");

        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Ударная дрель Bosch");

        assertThat(result).hasJsonPathBooleanValue("$.available");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();

        assertThat(result).hasJsonPathNumberValue("$.requestId");
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(42);
    }

    @Test
    void testSerializeWithNullRequestId() throws IOException {
        ItemCreateDto dto = ItemCreateDto.builder()
                .name("Отвертка")
                .description("Обычная крестовая отвертка")
                .available(false)
                .requestId(null)
                .build();

        JsonContent<ItemCreateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathValue("$.requestId").isNull();
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\n" +
                "  \"name\": \"Шуруповерт\",\n" +
                "  \"description\": \"Аккумуляторный шуруповерт\",\n" +
                "  \"available\": true,\n" +
                "  \"requestId\": 105\n" +
                "}";

        ItemCreateDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getName()).isEqualTo("Шуруповерт");
        assertThat(dto.getDescription()).isEqualTo("Аккумуляторный шуруповерт");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(105L);
    }

    @Test
    void testDeserializeWithoutRequestId() throws IOException {
        String jsonContent = "{\n" +
                "  \"name\": \"Пила\",\n" +
                "  \"description\": \"Ручная пила по дереву\",\n" +
                "  \"available\": false\n" +
                "}";

        ItemCreateDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getName()).isEqualTo("Пила");
        assertThat(dto.getDescription()).isEqualTo("Ручная пила по дереву");
        assertThat(dto.getAvailable()).isFalse();
        assertThat(dto.getRequestId()).isNull(); // Должно десериализоваться в null
    }
}