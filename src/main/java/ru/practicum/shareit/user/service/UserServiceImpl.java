package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;

@Component
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private UserMapper userMapper;

    @Override
    public Collection<UserResponseDto> findAllUsers() {
        return userMapper.toUserResponseDtoCollection(userStorage.findAllUsers());
    }

    @Override
    public UserResponseDto findUserById(Long id) {
        return userMapper.toUserResponseDto(userStorage.findUserById(id).orElseThrow(() -> {
            log.warn("Пользователь с id {} не найден", id);
            return new NotFoundException("Не найден пользователь с id " + id);
        }));
    }

    @Override
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        if (userStorage.existsByEmail(userCreateDto.getEmail())) {
            log.warn("Ошибка при добавлении пользователя, email {} уже существует", userCreateDto.getEmail());
            throw new EmailAlreadyExistsException("Пользователь с email " + userCreateDto.getEmail() + " уже существует");
        }
        User user = userMapper.toUserFromCreateDto(userCreateDto);
        UserResponseDto userResponseDto = userMapper.toUserResponseDto(userStorage.createUser(user));
        log.info("Добавлен пользователь с id {}", userResponseDto.getId());
        return userResponseDto;
    }

    @Override
    public UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto) {

        findUserById(userId);

        if (userStorage.existsByEmailAndIdNot(userUpdateDto.getEmail(), userId)) {
            log.warn("Ошибка при обновлении пользователя, email {} занят другим пользователем", userUpdateDto.getEmail());
            throw new EmailAlreadyExistsException("Email " + userUpdateDto.getEmail() + " занят другим пользователем");
        }

        User user = userMapper.toUserFromUpdateDto(userUpdateDto);
        UserResponseDto userResponseDto = userMapper.toUserResponseDto(userStorage.updateUser(userId, user));
        log.info("Обновлен пользователь с id {}", userResponseDto.getId());

        return userResponseDto;
    }

    @Override
    public void deleteUser(Long id) {
        userStorage.deleteUser(id);
        log.info("Удален пользователь с id {}", id);
    }

}
