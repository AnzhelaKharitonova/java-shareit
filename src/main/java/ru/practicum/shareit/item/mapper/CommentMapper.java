package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    Comment fromDto(CommentDto commentDto);

    @Mapping(source = "author.name", target = "authorName")
    @Mapping(source = "item.id", target = "itemId")
    CommentDto toDto(Comment comment);

    List<CommentDto> toDtoCollection(List<Comment> comments);
}
