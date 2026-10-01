package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.model.Request;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    Request fromDto(RequestCreateDto requestCreateDto);

    @Mapping(source = "requestor.name", target = "requestorName")
    RequestResponseDto toDto(Request request);

    List<RequestResponseDto> toDtoCollection(List<Request> requests);
}
