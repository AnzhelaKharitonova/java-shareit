package ru.practicum.shareit.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    User toUserFromCreateDto(UserCreateDto userCreateDto);

    void updateUserFromDto(UserUpdateDto dto, @MappingTarget User user);

    UserResponseDto toUserResponseDto(User user);

    Collection<UserResponseDto> toUserResponseDtoCollection(Collection<User> users);


}
