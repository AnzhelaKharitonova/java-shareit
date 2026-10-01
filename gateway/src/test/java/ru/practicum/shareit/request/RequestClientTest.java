package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.RequestCreateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(RequestClient.class)
class RequestClientTest {

    @Autowired
    private RequestClient requestClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    private RequestCreateDto createDto;

    @BeforeEach
    void setUp() {
        createDto = RequestCreateDto.builder()
                .description("Нужна щетка для обуви")
                .build();
    }

    @Test
    void createRequest_shouldSendPostRequest_andReturn201() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(createDto);

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/requests")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andExpect(content().json(jsonBody))
                .andRespond(withStatus(HttpStatus.CREATED).body("{\"id\":1,\"description\":\"Нужна щетка для обуви\"}").contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = requestClient.createRequest(1L, createDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        mockServer.verify();
    }

    @Test
    void findRequestsByUser_shouldSendGetRequest_toRootPath() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/requests")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "2"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = requestClient.findRequestsByUser(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void findAllRequests_shouldSendGetRequest_withPaginationParams() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/requests/all?from=0&size=20")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "3"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = requestClient.findAllRequests(3L, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void findRequestById_shouldSendGetRequest_withRequestIdInPath() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/requests/10")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "4"))
                .andRespond(withSuccess("{\"id\":10}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = requestClient.findRequestById(4L, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }
}
