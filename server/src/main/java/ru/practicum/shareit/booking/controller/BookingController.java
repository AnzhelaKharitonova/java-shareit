package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.util.Constants;

import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@AllArgsConstructor
@Validated
public class BookingController {
    private BookingService bookingService;

    @PostMapping
    public BookingResponseDto createBooking(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @RequestBody BookingCreateDto bookingCreateDto) {
        return bookingService.createBooking(bookingCreateDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @PathVariable("bookingId") Long bookingId,
            @RequestParam Boolean approved) {

        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto findBookingById(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @PathVariable("bookingId") Long bookingId) {

        return bookingService.findBookingById(userId, bookingId);
    }

    @GetMapping
    public Collection<BookingResponseDto> findBookingsByUser(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") State state,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {

        return bookingService.findBookingsByUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public Collection<BookingResponseDto> findBookingsByOwner(
            @RequestHeader(Constants.USER_ID_HEADER) Long ownerId,
            @RequestParam(defaultValue = "ALL") State state,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {

        return bookingService.findBookingsByOwner(ownerId, state, from, size);
    }

}
