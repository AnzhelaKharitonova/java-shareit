package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemToRequestDtoTest {

    @Autowired
    private JacksonTester<ItemToRequestDto> json;

    @Test
    void testSerialize() throws IOException {
        ItemToRequestDto dto = ItemToRequestDto.builder()
                .id(10L)
                .name("Дрель")
                .available(true)
                .ownerId(2L)
                .build();

        JsonContent<ItemToRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(10);

        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");

        assertThat(result).hasJsonPathBooleanValue("$.available");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();

        assertThat(result).hasJsonPathNumberValue("$.ownerId");
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(2);
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\n" +
                "  \"id\": 15,\n" +
                "  \"name\": \"Перфоратор\",\n" +
                "  \"available\": false,\n" +
                "  \"ownerId\": 4\n" +
                "}";

        ItemToRequestDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(15L);
        assertThat(dto.getName()).isEqualTo("Перфоратор");
        assertThat(dto.getAvailable()).isFalse();
        assertThat(dto.getOwnerId()).isEqualTo(4L);
    }
}
