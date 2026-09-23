package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.State;

import java.util.Collection;

@Service
public interface BookingService {

    BookingResponseDto createBooking(BookingCreateDto bookingCreateDto, Long userId);

    BookingResponseDto approveBooking(Long userId, Long bookingId, Boolean isApproved);

    BookingResponseDto findBookingById(Long userId, Long bookingId);

    Collection<BookingResponseDto> findBookingsByUser(Long userId, State state);

    Collection<BookingResponseDto> findBookingsByOwner(Long ownerId, State state);
}
