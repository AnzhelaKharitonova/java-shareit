package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createBooking_ShouldReturnBookingAndStatus200() throws Exception {
        // Given
        BookingCreateDto createDto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(10L)
                .build();

        when(bookingService.createBooking(any(BookingCreateDto.class), eq(1L)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(bookingService).createBooking(any(BookingCreateDto.class), eq(1L));
    }

    @Test
    void approveBooking_ShouldReturnUpdatedBooking() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(10L)
                .build();

        when(bookingService.approveBooking(1L, 10L, true))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/10")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(bookingService).approveBooking(1L, 10L, true);
    }

    @Test
    void findBookingById_ShouldReturnBooking() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(10L)
                .build();

        when(bookingService.findBookingById(1L, 10L))
                .thenReturn(responseDto);

        mockMvc.perform(get("/bookings/10")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(bookingService).findBookingById(1L, 10L);
    }

    @Test
    void findBookingsByUser_ShouldReturnCollection() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder().id(10L).build();
        when(bookingService.findBookingsByUser(1L, State.ALL, 0, 10))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));

        verify(bookingService).findBookingsByUser(1L, State.ALL, 0, 10);
    }

    @Test
    void findBookingsByOwner_ShouldReturnCollectionWithDefaultParams() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder().id(20L).build();
        when(bookingService.findBookingsByOwner(1L, State.ALL, 0, 10))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20));

        verify(bookingService).findBookingsByOwner(1L, State.ALL, 0, 10);
    }
}
