package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemResponseDtoTest {

    @Autowired
    private JacksonTester<ItemResponseDto> json;

    @Test
    void testSerialize() throws IOException {
        LocalDateTime lastStart = LocalDateTime.of(2026, 10, 1, 10, 0, 0);
        LocalDateTime lastEnd = LocalDateTime.of(2026, 10, 1, 11, 0, 0);
        LocalDateTime nextStart = LocalDateTime.of(2026, 10, 2, 14, 0, 0);
        LocalDateTime nextEnd = LocalDateTime.of(2026, 10, 2, 15, 0, 0);

        BookingShortDto lastBooking = BookingShortDto.builder()
                .start(lastStart)
                .end(lastEnd)
                .build();

        BookingShortDto nextBooking = BookingShortDto.builder()
                .start(nextStart)
                .end(nextEnd)
                .build();

        CommentDto comment = CommentDto.builder()
                .id(1L)
                .text("Отличная дрель!")
                .authorName("Алексей")
                .itemId(10L)
                .created(LocalDateTime.of(2026, 10, 1, 12, 0, 0))
                .build();

        ItemResponseDto dto = ItemResponseDto.builder()
                .id(10L)
                .name("Дрель")
                .description("Мощный инструмент")
                .available(true)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(comment))
                .build();

        JsonContent<ItemResponseDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(10);

        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");

        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Мощный инструмент");

        assertThat(result).hasJsonPathBooleanValue("$.available");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();

        assertThat(result).hasJsonPathMapValue("$.lastBooking");
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.start").startsWith("2026-10-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.end").startsWith("2026-10-01T11:00:00");

        assertThat(result).hasJsonPathMapValue("$.nextBooking");
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.start").startsWith("2026-10-02T14:00:00");
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.end").startsWith("2026-10-02T15:00:00");

        assertThat(result).hasJsonPathArrayValue("$.comments");
        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Отличная дрель!");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo("Алексей");
    }

    @Test
    void testSerializeWithNullBookingsAndEmptyComments() throws IOException {
        ItemResponseDto dto = ItemResponseDto.builder()
                .id(11L)
                .name("Отвертка")
                .description("Обычная")
                .available(false)
                .lastBooking(null)
                .nextBooking(null)
                .comments(List.of())
                .build();

        JsonContent<ItemResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathValue("$.lastBooking").isNull();
        assertThat(result).extractingJsonPathValue("$.nextBooking").isNull();
        assertThat(result).hasJsonPathArrayValue("$.comments");
        assertThat(result).extractingJsonPathArrayValue("$.comments").isEmpty();
    }
}

