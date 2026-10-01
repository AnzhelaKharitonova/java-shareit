package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemToRequestDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ItemMapper {

    Item toItemFromCreateDto(ItemCreateDto itemCreateDto);

    void updateItemFromDto(ItemUpdateDto dto, @MappingTarget Item item);

    ItemResponseDto toItemResponseDto(Item item);

    List<ItemResponseDto> toItemResponseDtoCollection(List<Item> items);

    @Mapping(source = "owner.id", target = "ownerId")
    ItemToRequestDto toRequestDto(Item item);

    List<ItemToRequestDto> toRequestDtoCollection(List<Item> items);

}
