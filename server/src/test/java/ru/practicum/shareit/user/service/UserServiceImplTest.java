package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserCreateDto createDto;
    private UserResponseDto responseDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Иван")
                .email("ivan@test.com")
                .build();

        createDto = UserCreateDto.builder()
                .name("Иван")
                .email("ivan@test.com")
                .build();

        responseDto = UserResponseDto.builder()
                .id(1L)
                .name("Иван")
                .email("ivan@test.com")
                .build();
    }

    @Test
    void findAllUsers_ShouldReturnCollectionWithPagination() {
        // Given
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toUserResponseDtoCollection(anyList())).thenReturn(List.of(responseDto));

        Collection<UserResponseDto> result = userService.findAllUsers(0, 10);

        assertThat(result).hasSize(1).contains(responseDto);
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void findUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.findUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findUserById_WhenUserNotExists_ShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Не найден пользователь с id 1");
    }

    @Test
    void createUser_WhenEmailIsUnique_ShouldSaveUser() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toUserFromCreateDto(createDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.createUser(createDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("ivan@test.com");
        verify(userRepository).save(user);
    }

    @Test
    void createUser_WhenEmailAlreadyExists_ShouldThrowEmailAlreadyExistsException() {
        when(userRepository.existsByEmail("ivan@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createDto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("уже существует");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WithValidNewEmail_ShouldUpdateSuccessfully() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Иван Обновленный")
                .email("new_ivan@test.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("new_ivan@test.com", 1L)).thenReturn(false);
        when(userMapper.toUserResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(1L, updateDto);

        assertThat(result).isNotNull();
        verify(userMapper).updateUserFromDto(updateDto, user);
    }

    @Test
    void updateUser_WhenEmailIsTakenByAnotherUser_ShouldThrowEmailAlreadyExistsException() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("taken@test.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("taken@test.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, updateDto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("занят другим пользователем");

        verify(userMapper, never()).updateUserFromDto(any(), any());
    }

    @Test
    void updateUser_WithoutEmailChange_ShouldUpdateSuccessfully() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Только Имя")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(1L, updateDto);

        assertThat(result).isNotNull();
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(userMapper).updateUserFromDto(updateDto, user);
    }

    @Test
    void deleteUser_ShouldCallRepositoryDelete() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}
