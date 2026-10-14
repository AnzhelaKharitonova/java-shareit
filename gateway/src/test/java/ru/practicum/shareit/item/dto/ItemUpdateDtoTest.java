package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemUpdateDtoTest {

    @Autowired
    private JacksonTester<ItemUpdateDto> json;

    @Test
    void testSerialize() throws IOException {
        // Given
        ItemUpdateDto dto = ItemUpdateDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Новое описание для дрели")
                .available(false)
                .build();

        JsonContent<ItemUpdateDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");

        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Новое описание для дрели");

        assertThat(result).hasJsonPathBooleanValue("$.available");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isFalse();
    }

    @Test
    void testDeserializeAllFields() throws IOException {
        String jsonContent = "{\n" +
                "  \"id\": 2,\n" +
                "  \"name\": \"Шуруповерт\",\n" +
                "  \"description\": \"Мощный шуруповерт\",\n" +
                "  \"available\": true\n" +
                "}";

        ItemUpdateDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("Шуруповерт");
        assertThat(dto.getDescription()).isEqualTo("Мощный шуруповерт");
        assertThat(dto.getAvailable()).isTrue();
    }

    @Test
    void testDeserializePartialFields() throws IOException {
        String jsonContent = "{\n" +
                "  \"available\": false\n" +
                "}";

        ItemUpdateDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getAvailable()).isFalse();
        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isNull();
        assertThat(dto.getDescription()).isNull();
    }
}
