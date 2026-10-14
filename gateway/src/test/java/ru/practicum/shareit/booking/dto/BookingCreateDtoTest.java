package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    @Test
    void testSerialize() throws IOException {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 12, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 2, 12, 0, 0);

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        // When
        JsonContent<BookingCreateDto> result = json.write(dto);

        // Then
        assertThat(result).hasJsonPathNumberValue("$.itemId");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);

        // Проверяем формат дат (по умолчанию Jackson сериализует LocalDateTime в ISO-8601 строку)
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).extractingJsonPathStringValue("$.start").startsWith("2026-10-01T12:00:00");

        assertThat(result).hasJsonPathStringValue("$.end");
        assertThat(result).extractingJsonPathStringValue("$.end").startsWith("2026-10-02T12:00:00");
    }

    @Test
    void testDeserialize() throws IOException {
        // Given
        String jsonContent = "{\n" +
                "  \"itemId\": 1,\n" +
                "  \"start\": \"2026-10-01T12:00:00\",\n" +
                "  \"end\": \"2026-10-02T12:00:00\"\n" +
                "}";

        // When
        BookingCreateDto dto = json.parse(jsonContent).getObject();

        // Then
        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 10, 1, 12, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 10, 2, 12, 0, 0));

        // Дополнительно проверяем работу кастомной валидации после десериализации
        assertThat(dto.isValidBookingPeriod()).isTrue();
    }

    @Test
    void testDeserializeWithInvalidPeriod() throws IOException {
        // Given: дата конца раньше даты начала
        String jsonContent = "{\n" +
                "  \"itemId\": 5,\n" +
                "  \"start\": \"2026-10-02T12:00:00\",\n" +
                "  \"end\": \"2026-10-01T12:00:00\"\n" +
                "}";

        // When
        BookingCreateDto dto = json.parse(jsonContent).getObject();

        // Then
        assertThat(dto.getItemId()).isEqualTo(5L);
        // Метод кастомной валидации должен вернуть false, так как период некорректен
        assertThat(dto.isValidBookingPeriod()).isFalse();
    }
}