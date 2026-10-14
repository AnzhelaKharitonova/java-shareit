package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingResponseDtoTest {

    @Autowired
    private JacksonTester<BookingResponseDto> json;

    @Test
    void testSerialize() throws IOException {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 12, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 2, 12, 0, 0);

        // Наполняем вложенный ItemResponseDto согласно его структуре
        ItemResponseDto itemDto = ItemResponseDto.builder()
                .id(10L)
                .name("Дрель")
                .description("Мощный инструмент")
                .available(true)
                .comments(List.of())
                .build();

        // Наполняем вложенный UserResponseDto согласно его структуре
        UserResponseDto bookerDto = UserResponseDto.builder()
                .id(3L)
                .name("Алексей")
                .email("alex@example.com")
                .build();

        BookingResponseDto dto = BookingResponseDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(itemDto)
                .booker(bookerDto)
                .status(Status.APPROVED) // Замените на доступный элемент вашего enum Status
                .build();

        // When
        JsonContent<BookingResponseDto> result = json.write(dto);

        // Then
        // Проверяем плоские поля бронирования
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).extractingJsonPathStringValue("$.start").startsWith("2026-10-01T12:00:00");

        assertThat(result).hasJsonPathStringValue("$.end");
        assertThat(result).extractingJsonPathStringValue("$.end").startsWith("2026-10-02T12:00:00");

        assertThat(result).hasJsonPathStringValue("$.status");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");

        // Проверяем вложенную сущность вещи (item)
        assertThat(result).hasJsonPathMapValue("$.item");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isTrue();

        // Проверяем вложенную сущность пользователя (booker)
        assertThat(result).hasJsonPathMapValue("$.booker");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Алексей");
        assertThat(result).extractingJsonPathStringValue("$.booker.email").isEqualTo("alex@example.com");
    }
}
