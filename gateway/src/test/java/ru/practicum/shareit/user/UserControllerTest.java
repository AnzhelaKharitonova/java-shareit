package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerGatewayTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void findAllUsers_WhenValidDefaults_ReturnsOk() throws Exception {
        when(userClient.findAllUsers(anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void findAllUsers_WhenNegativeFrom_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/users")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAllUsers_WhenZeroSize_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/users")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WhenValid_ReturnsOk() throws Exception {
        UserCreateDto validDto = UserCreateDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .build();

        when(userClient.createUser(any(UserCreateDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_WhenNameIsBlank_ReturnsBadRequest() throws Exception {
        UserCreateDto invalidDto = UserCreateDto.builder()
                .name("")
                .email("ivan@example.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void createUser_WhenEmailIsInvalid_ReturnsBadRequest() throws Exception {
        UserCreateDto invalidDto = UserCreateDto.builder()
                .name("Иван")
                .email("not-an-email")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void updateUser_WhenEmailIsInvalid_ReturnsBadRequest() throws Exception {
        UserUpdateDto invalidDto = UserUpdateDto.builder()
                .email("wrong_format.com")
                .build();

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void updateUser_WhenValidPartialFields_ReturnsOk() throws Exception {
        UserUpdateDto validDto = UserUpdateDto.builder()
                .name("Новое Имя")
                .build();

        when(userClient.updateUser(anyLong(), any(UserUpdateDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_WhenValidId_ReturnsOk() throws Exception {
        when(userClient.deleteUser(anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}
