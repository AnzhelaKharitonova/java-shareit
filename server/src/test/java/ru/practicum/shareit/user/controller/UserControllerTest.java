package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerServerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void findAllUsers_ShouldReturnCollectionWithParams() throws Exception {
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("Иван")
                .email("ivan@example.com")
                .build();

        when(userService.findAllUsers(0, 10)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Иван"))
                .andExpect(jsonPath("$[0].email").value("ivan@example.com"));

        verify(userService).findAllUsers(0, 10);
    }

    @Test
    void findUser_ShouldReturnUser() throws Exception {
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(2L)
                .name("Мария")
                .email("maria@example.com")
                .build();

        when(userService.findUserById(2L)).thenReturn(responseDto);

        mockMvc.perform(get("/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Мария"));

        verify(userService).findUserById(2L);
    }

    @Test
    void createUser_ShouldReturnCreatedUserAndStatus200() throws Exception {
        UserCreateDto createDto = UserCreateDto.builder()
                .name("Алексей")
                .email("alex@example.com")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(3L)
                .name("Алексей")
                .email("alex@example.com")
                .build();

        when(userService.createUser(any(UserCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Алексей"));

        verify(userService).createUser(any(UserCreateDto.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Алексей Обновленный")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(3L)
                .name("Алексей Обновленный")
                .email("alex@example.com")
                .build();

        when(userService.updateUser(eq(3L), any(UserUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/users/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Алексей Обновленный"));

        verify(userService).updateUser(eq(3L), any(UserUpdateDto.class));
    }

    @Test
    void deleteUser_ShouldReturnStatus200() throws Exception {
        mockMvc.perform(delete("/users/4"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(4L);
    }
}
