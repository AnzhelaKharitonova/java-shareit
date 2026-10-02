package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.State;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(BookingClient.class)
class BookingClientTest {

    @Autowired
    private BookingClient bookingClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingCreateDto createDto;

    @BeforeEach
    void setUp() {
        createDto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2026, 10, 1, 12, 0))
                .end(LocalDateTime.of(2026, 10, 2, 12, 0))
                .build();
    }

    @Test
    void createBooking_shouldSendPostRequest_andReturn201() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(createDto);

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/bookings")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andExpect(content().json(jsonBody))
                .andRespond(withStatus(HttpStatus.CREATED).body("{\"id\":1}").contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = bookingClient.createBooking(1L, createDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        mockServer.verify();
    }

    @Test
    void findBookingById_shouldSendGetRequest_withUserHeader() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/bookings/10")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "2"))
                .andRespond(withSuccess("{\"id\":10}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = bookingClient.findBookingById(2L, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void approveBooking_shouldSendPatchRequest_withQueryParam_andNoBody() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/bookings/10?approved=true")))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andExpect(content().string("")) // Проверяем, что тело запроса пустое (null)
                .andRespond(withSuccess("{\"id\":10,\"status\":\"APPROVED\"}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = bookingClient.approveBooking(1L, 10L, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void findBookingsByUser_shouldSendGetRequest_withStateAndPaginationParams() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString(
                "/bookings?state=FUTURE&from=0&size=10")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "5"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = bookingClient.findBookingsByUser(5L, State.FUTURE, 0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void findBookingsByOwner_shouldSendGetRequest_toOwnerEndpoint_withStateAndPaginationParams() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString(
                "/bookings/owner?state=ALL&from=5&size=5")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "3"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = bookingClient.findBookingsByOwner(3L, State.ALL, 5, 5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }
}
