package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreateDto {

    @NotNull(message = "Дата начала бронирования должна быть указана")
    @Future(message = "Дата начала бронирования не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "Дата конца бронирования должна быть указана")
    private LocalDateTime end;

    @NotNull(message = "Id вещи должен быть указан")
    private Long itemId;

    @AssertTrue(message = "Дата конца бронирования должна быть строго позже начала бронирования")
    public boolean isValidBookingPeriod() {
        if (start == null || end == null) {
            return true;
        }
        return end.isAfter(start);
    }

}
