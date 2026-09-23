package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Component
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private UserMapper userMapper;

    @Override
    public Collection<UserResponseDto> findAllUsers() {
        return userMapper.toUserResponseDtoCollection(userRepository.findAll());
    }

    @Override
    public UserResponseDto findUserById(Long id) {
        return userMapper.toUserResponseDto(userRepository.findById(id).orElseThrow(() -> {
            log.warn("Пользователь с id {} не найден", id);
            return new NotFoundException("Не найден пользователь с id " + id);
        }));
    }

    @Override
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            log.warn("Ошибка при добавлении пользователя, email {} уже существует", userCreateDto.getEmail());
            throw new EmailAlreadyExistsException("Пользователь с email " + userCreateDto.getEmail() + " уже существует");
        }
        User user = userMapper.toUserFromCreateDto(userCreateDto);
        UserResponseDto userResponseDto = userMapper.toUserResponseDto(userRepository.save(user));
        log.info("Добавлен пользователь с id {}", userResponseDto.getId());
        return userResponseDto;
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto) {

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь с id {} не найден", userId);
            return new NotFoundException("Не найден пользователь с id " + userId);
        });
        String newEmail = userUpdateDto.getEmail();
        if (newEmail != null && !newEmail.isBlank()) {
            if (userRepository.existsByEmailAndIdNot(userUpdateDto.getEmail(), userId)) {
                log.warn("Ошибка при обновлении пользователя, email {} занят другим пользователем",
                        userUpdateDto.getEmail());
                throw new EmailAlreadyExistsException("Email " + userUpdateDto.getEmail() +
                        " занят другим пользователем");
            }
        }
        userMapper.updateUserFromDto(userUpdateDto, user);
        UserResponseDto userResponseDto = userMapper.toUserResponseDto(user);
        log.info("Обновлен пользователь с id {}", userResponseDto.getId());

        return userResponseDto;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        log.info("Удален пользователь с id {}", id);
    }

}
