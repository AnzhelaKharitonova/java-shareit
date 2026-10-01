package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemToRequestDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class RequestResponseDtoTest {

    @Autowired
    private JacksonTester<RequestResponseDto> json;

    @Test
    void testSerialize() throws IOException {
        // Given
        LocalDateTime createdTime = LocalDateTime.of(2026, 10, 1, 16, 0, 0);

        // Создаем заглушку для вложенного DTO (замените на реальные поля вашего ItemToRequestDto)
        ItemToRequestDto mockItem = ItemToRequestDto.builder()
                .id(10L)
                .name("Дрель")
                .available(true)
                .build();

        RequestResponseDto dto = RequestResponseDto.builder()
                .id(1L)
                .description("Нужен инструмент для ремонта")
                .requestorName("Алексей")
                .created(createdTime)
                .items(List.of(mockItem))
                .build();

        // When
        JsonContent<RequestResponseDto> result = json.write(dto);

        // Then
        // Проверяем базовые поля
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужен инструмент для ремонта");

        assertThat(result).hasJsonPathStringValue("$.requestorName");
        assertThat(result).extractingJsonPathStringValue("$.requestorName").isEqualTo("Алексей");

        // Проверяем ISO формат даты по умолчанию (yyyy-MM-ddTHH:mm:ss)
        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).extractingJsonPathStringValue("$.created").startsWith("2026-10-01T16:00:00");

        // Проверяем структуру вложенного списка items
        assertThat(result).hasJsonPathArrayValue("$.items");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.items[0].available").isTrue();
    }
}
