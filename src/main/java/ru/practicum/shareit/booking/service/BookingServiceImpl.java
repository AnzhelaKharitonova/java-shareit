package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private BookingMapper bookingMapper;

    @Override
    public BookingResponseDto createBooking(BookingCreateDto bookingCreateDto, Long userId) {
        User booker = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("При бронировании пользователь с id {} не найден", userId);
            return new NotFoundException("При бронировании не найден пользователь с id " + userId);
        });
        Item item = itemRepository.findById(bookingCreateDto.getItemId()).orElseThrow(() -> {
            log.warn("При бронировании вещь с id {} не найдена", bookingCreateDto.getItemId());
            return new NotFoundException("При бронировании не найдена вещь с id " + bookingCreateDto.getItemId());
        });
        if (item.getAvailable() == false) {
            throw new ValidationException("Вещь с id " + item.getId() + " недоступна для бронирования в данный момент");
        }
        Booking booking = bookingMapper.toBookingFromCreateDto(bookingCreateDto);

        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);

        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long userId, Long bookingId, Boolean approved) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            log.warn("Бронирование с id {} не найдено", bookingId);
            return new NotFoundException("Не найден бронирование с id " + bookingId);
        });
        if (!userId.equals(booking.getItem().getOwner().getId())) {
            log.warn("Запрос на подтверждение бронирования отправлен не владельцем вещи");
            throw new NotAvailableException("Бронирование может подтвердить только владелец вещи");
        }
        Status status;
        if (approved) {
            status = Status.APPROVED;
        } else {
            status = Status.REJECTED;
        }
        booking.setStatus(status);
        return bookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public BookingResponseDto findBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            log.warn("Бронирование с id {} не найдено", bookingId);
            return new NotFoundException("Не найдено бронирование с id " + bookingId);
        });
        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            log.warn("Запрос на просмотр деталей бронирования не автором или владельцем вещи");
            throw new NotAvailableException("Просмотр деталей бронирования доступен только его автору или владельцу вещи");
        }

        return bookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> findBookingsByUser(Long userId, State state) {
        List<Booking> bookings = bookingRepository.findByBookerId(userId);
        if (bookings.isEmpty()) {
            return Collections.emptyList();
        }
        List<Booking> sortedBookingsByState = sortBookingsByState(bookings, state);

        return bookingMapper.toBookingResponseDtoCollection(sortedBookingsByState).stream()
                .sorted(Comparator.comparing(BookingResponseDto::getStart)).toList();
    }

    @Override
    public List<BookingResponseDto> findBookingsByOwner(Long ownerId, State state) {
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        if (items.isEmpty()) {
            log.warn("У пользователя с id {} не найдено вещей для бронирования", ownerId);
            throw new NotFoundException("У вас нет вещей доступных для бронирования");
        }
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<Booking> bookings = bookingRepository.findByItemIdIn(itemIds);
        if (bookings.isEmpty()) {
            return Collections.emptyList();
        }
        List<Booking> sortedBookingsByState = sortBookingsByState(bookings, state);

        return bookingMapper.toBookingResponseDtoCollection(sortedBookingsByState).stream()
                .sorted(Comparator.comparing(BookingResponseDto::getStart)).toList();
    }

    private List<Booking> sortBookingsByState(List<Booking> bookings, State state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case State.ALL -> bookings;
            case State.CURRENT -> bookings.stream()
                    .filter(booking -> booking.getStatus() == Status.APPROVED)
                    .filter(booking ->
                            booking.getStart().isBefore(now)
                                    && booking.getEnd().isAfter(now))
                    .toList();
            case State.PAST -> bookings.stream()
                    .filter(booking -> booking.getStatus() == Status.APPROVED)
                    .filter(booking -> booking.getEnd().isBefore(now))
                    .toList();
            case State.FUTURE -> bookings.stream()
                    .filter(booking -> booking.getStatus() == Status.APPROVED
                            || booking.getStatus() == Status.WAITING)
                    .filter(booking -> booking.getStart().isAfter(now))
                    .toList();
            case State.WAITING -> bookings.stream()
                    .filter(booking -> booking.getStatus() == Status.WAITING)
                    .toList();
            case State.REJECTED -> bookings.stream()
                    .filter(booking -> booking.getStatus() == Status.REJECTED)
                    .toList();
            default -> throw new NotFoundException("Неизвестный статус бронирования " + state);
        };
    }
}

