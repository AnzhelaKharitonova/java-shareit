package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item toItemFromCreateDto(ItemCreateDto itemCreateDto);

    Item toItemFromUpdateDto(ItemUpdateDto itemUpdateDto);

    ItemResponseDto toItemResponseDto(Item item);

    Collection<ItemResponseDto> toItemResponseDtoCollection(Collection<Item> items);

}
