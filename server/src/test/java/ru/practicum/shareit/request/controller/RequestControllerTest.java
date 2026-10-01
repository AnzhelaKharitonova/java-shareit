package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
class RequestControllerServerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestService requestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createRequest_ShouldReturnRequestAndStatus200() throws Exception {
        RequestCreateDto createDto = RequestCreateDto.builder()
                .description("Нужна стремянка")
                .build();

        RequestResponseDto responseDto = RequestResponseDto.builder()
                .id(1L)
                .description("Нужна стремянка")
                .build();

        when(requestService.createRequest(eq(1L), any(RequestCreateDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна стремянка"));

        verify(requestService).createRequest(eq(1L), any(RequestCreateDto.class));
    }

    @Test
    void findRequestsByUser_ShouldReturnList() throws Exception {
        // Given
        RequestResponseDto responseDto = RequestResponseDto.builder()
                .id(1L)
                .description("Нужен перфоратор")
                .build();

        when(requestService.findRequestsByUser(1L)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Нужен перфоратор"));

        verify(requestService).findRequestsByUser(1L);
    }

    @Test
    void findAllRequests_ShouldReturnListWithParams() throws Exception {
        // Given
        RequestResponseDto responseDto = RequestResponseDto.builder()
                .id(2L)
                .description("Ищу газонокосилку")
                .build();

        when(requestService.findAllRequests(1L, 0, 10)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].description").value("Ищу газонокосилку"));

        verify(requestService).findAllRequests(1L, 0, 10);
    }

    @Test
    void findRequestById_ShouldReturnRequest() throws Exception {
        RequestResponseDto responseDto = RequestResponseDto.builder()
                .id(5L)
                .description("Запрос на дрель")
                .build();

        when(requestService.findRequestById(5L)).thenReturn(responseDto);

        mockMvc.perform(get("/requests/5")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.description").value("Запрос на дрель"));

        verify(requestService).findRequestById(5L);
    }
}
