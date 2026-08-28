package ru.practicum.shareit.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUserFromCreateDto(UserCreateDto userCreateDto);

    User toUserFromUpdateDto(UserUpdateDto userUpdateDto);

    UserResponseDto toUserResponseDto(User user);

    Collection<UserResponseDto> toUserResponseDtoCollection(Collection<User> users);


}
