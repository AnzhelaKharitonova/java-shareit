package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingCreateDto createDto;
    private BookingResponseDto responseDto;

    @BeforeEach
    void setUp() {
        booker = User.builder().id(1L).name("Booker").email("booker@test.com").build();
        owner = User.builder().id(2L).name("Owner").email("owner@test.com").build();

        item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description(" Bosch")
                .available(true)
                .owner(owner)
                .build();

        createDto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(createDto.getStart())
                .end(createDto.getEnd())
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();

        responseDto = BookingResponseDto.builder()
                .id(1L)
                .status(Status.WAITING)
                .build();
    }

    @Test
    void createBooking_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingMapper.toBookingFromCreateDto(createDto)).thenReturn(new Booking());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(responseDto);

        BookingResponseDto result = bookingService.createBooking(createDto, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(createDto, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("При бронировании не найден пользователь с id 1");
    }

    @Test
    void createBooking_ItemNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(createDto, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("При бронировании не найдена вещь с id 1");
    }

    @Test
    void createBooking_ItemNotAvailable_ThrowsValidationException() {
        item.setAvailable(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(createDto, 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("недоступна для бронирования");
    }

    @Test
    void approveBooking_Approve_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        responseDto.setStatus(Status.APPROVED);
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(responseDto);

        BookingResponseDto result = bookingService.approveBooking(2L, 1L, true);

        assertThat(result.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(booking.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void approveBooking_Reject_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        responseDto.setStatus(Status.REJECTED);
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(responseDto);

        BookingResponseDto result = bookingService.approveBooking(2L, 1L, false);

        assertThat(result.getStatus()).isEqualTo(Status.REJECTED);
        assertThat(booking.getStatus()).isEqualTo(Status.REJECTED);
    }

    @Test
    void approveBooking_NotOwner_ThrowsNotAvailableException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(3L, 1L, true))
                .isInstanceOf(NotAvailableException.class)
                .hasMessageContaining("Бронирование может подтвердить только владелец вещи");
    }

    @Test
    void findBookingById_ByBookerOrOwner_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(responseDto);

        BookingResponseDto resultByBooker = bookingService.findBookingById(1L, 1L);
        assertThat(resultByBooker).isNotNull();

        BookingResponseDto resultByOwner = bookingService.findBookingById(2L, 1L);
        assertThat(resultByOwner).isNotNull();
    }

    @Test
    void findBookingById_ByStranger_ThrowsNotAvailableException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.findBookingById(3L, 1L))
                .isInstanceOf(NotAvailableException.class)
                .hasMessageContaining("Просмотр деталей бронирования доступен только его автору");
    }

    @Test
    void findBookingsByUser_StateAll_CallsCorrectRepositoryMethod() {
        when(bookingRepository.findByBookerId(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(booking));

        bookingService.findBookingsByUser(1L, State.ALL, 0, 10);

        verify(bookingRepository).findByBookerId(eq(1L), any(Pageable.class));
    }

    @Test
    void findBookingsByUser_StateWaiting_CallsCorrectRepositoryMethod() {
        when(bookingRepository.findByBookerIdAndStatus(eq(1L), eq(Status.WAITING), any(Pageable.class)))
                .thenReturn(List.of(booking));

        bookingService.findBookingsByUser(1L, State.WAITING, 0, 10);

        verify(bookingRepository).findByBookerIdAndStatus(eq(1L), eq(Status.WAITING), any(Pageable.class));
    }

    @Test
    void findBookingsByOwner_NoItemsFound_ThrowsNotFoundException() {
        when(itemRepository.findByOwnerId(2L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> bookingService.findBookingsByOwner(2L, State.ALL, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("У вас нет вещей доступных для бронирования");
    }

    @Test
    void findBookingsByOwner_StateFuture_CallsCorrectRepositoryMethod() {
        when(itemRepository.findByOwnerId(2L)).thenReturn(List.of(item));
        when(bookingRepository.findByItemIdInAndStartAfter(anyList(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        bookingService.findBookingsByOwner(2L, State.FUTURE, 0, 10);

        verify(bookingRepository).findByItemIdInAndStartAfter(anyList(), any(LocalDateTime.class), any(Pageable.class));
    }
}
