package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.RequestCreateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
class RequestControllerGatewayTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestClient requestClient;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createRequest_WhenValid_ReturnsOk() throws Exception {
        RequestCreateDto validDto = RequestCreateDto.builder()
                .description("Нужна стремянка на 3 дня")
                .build();

        when(requestClient.createRequest(anyLong(), any(RequestCreateDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());
    }

    @Test
    void createRequest_WhenDescriptionIsBlank_ReturnsBadRequest() throws Exception {
        RequestCreateDto invalidDto = RequestCreateDto.builder()
                .description("   ")
                .build();

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    void findAllRequests_WhenValidDefaults_ReturnsOk() throws Exception {
        when(requestClient.findAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void findAllRequests_WhenNegativeFrom_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAllRequests_WhenZeroSize_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findRequestById_WhenValid_ReturnsOk() throws Exception {
        when(requestClient.findRequestById(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(get("/requests/5")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }
}
