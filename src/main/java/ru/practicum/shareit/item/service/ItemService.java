package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collection;

public interface ItemService {

    ItemResponseDto createItem(ItemCreateDto itemCreateDto, Long userId);

    ItemResponseDto updateItem(Long itemId, Long userId, ItemUpdateDto itemUpdateDto);

    ItemResponseDto findItemById(Long itemId);

    Collection<ItemResponseDto> findItemsByOwner(Long userId);

    Collection<ItemResponseDto> search(String text);
}
