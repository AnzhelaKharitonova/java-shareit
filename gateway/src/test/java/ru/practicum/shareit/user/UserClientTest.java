package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(UserClient.class)
class UserClientTest {

    @Autowired
    private UserClient userClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    private UserCreateDto createDto;
    private UserUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        createDto = UserCreateDto.builder()
                .name("Alex")
                .email("alex@test.com")
                .build();

        updateDto = UserUpdateDto.builder()
                .name("Alex New")
                .build();
    }

    @Test
    void createUser_shouldSendPostRequest_andReturn201() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(createDto);

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/users")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(jsonBody))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .body("{\"id\":1,\"name\":\"Alex\",\"email\":\"alex@test.com\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = userClient.createUser(createDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        mockServer.verify();
    }

    @Test
    void updateUser_shouldSendPatchRequest_withUserIdInPath() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(updateDto);

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/users/1")))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(content().json(jsonBody))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"Alex New\"}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = userClient.updateUser(1L, updateDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void findUserById_shouldSendGetRequest_withUserId() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/users/1")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = userClient.findUserById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void findAllUsers_shouldSendGetRequest_withPaginationParams() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/users/?from=0&size=5")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = userClient.findAllUsers(0, 5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void deleteUser_shouldSendDeleteRequest_withUserId() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/users/1")))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        ResponseEntity<Object> response = userClient.deleteUser(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }
}
