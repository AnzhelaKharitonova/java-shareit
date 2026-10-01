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
class BookingShortDtoTest {

    @Autowired
    private JacksonTester<BookingShortDto> json;

    @Test
    void testSerialize() throws IOException {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 12, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 2, 12, 0, 0);

        BookingShortDto dto = BookingShortDto.builder()
                .start(start)
                .end(end)
                .build();

        // When
        JsonContent<BookingShortDto> result = json.write(dto);

        // Then
        // Проверяем ISO-8601 формат сериализации дат по умолчанию (yyyy-MM-ddTHH:mm:ss)
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).extractingJsonPathStringValue("$.start").startsWith("2026-10-01T12:00:00");

        assertThat(result).hasJsonPathStringValue("$.end");
        assertThat(result).extractingJsonPathStringValue("$.end").startsWith("2026-10-02T12:00:00");
    }

    @Test
    void testDeserialize() throws IOException {
        // Given
        String jsonContent = "{\n" +
                "  \"start\": \"2026-10-01T12:00:00\",\n" +
                "  \"end\": \"2026-10-02T12:00:00\"\n" +
                "}";

        // When
        BookingShortDto dto = json.parse(jsonContent).getObject();

        // Then
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 10, 1, 12, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 10, 2, 12, 0, 0));
    }
}
