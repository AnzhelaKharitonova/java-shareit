package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Collection;

public interface UserService {

    Collection<UserResponseDto> findAllUsers();

    UserResponseDto findUserById(Long id);

    UserResponseDto createUser(UserCreateDto userCreateDto);

    UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto);

    void deleteUser(Long id);
}
