package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    Booking toBookingFromCreateDto(BookingCreateDto bookingCreateDto);

    BookingResponseDto toBookingResponseDto(Booking booking);

    Collection<BookingResponseDto> toBookingResponseDtoCollection(Collection<Booking> bookings);

    BookingShortDto toBookingShortDto(Booking booking);
}
